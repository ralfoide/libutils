#!/bin/bash
#
# Inserts all LibUtils libs.versions.toml entries in the target file if missing.

# SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
SRC="${SCRIPT_DIR}/gradle/libs.versions.toml"

DRY="echo"
DST=""

# Parse flags
while [[ $# -gt 0 ]]; do
  case "$1" in
    -f)
        DRY=""
        shift
        ;;
    *)
        DST="$1"
        shift
        ;;
  esac
done


NEW="${DST}.new"
if [[ -z "$DST" || ! -f "$DST" || ! "$DST"  =~ libs.versions.toml$ ]]; then
  echo "Usage: $0 [-f] top-level-project/gradle/libs.versions.toml"
  echo "  -f : Apply changes to file (default is dry-run mode)"
  echo
  echo "This populates the target libs.versions.toml with all entries needed"
  echo "by LibUtils if not already defined."
  echo
  exit 1
fi

if [[ ! -f "$SRC" ]]; then
  echo "Error: Source not found at $SRC"
  echo
  exit 1
fi


function read_keys() {
  local NAME="$1"
  local FILE="$2"
  local -n ARR="$3"   # reference array from its name
  local LINE
  local KEY
  local SECTION=""
  local SECTION_FOUND=""

  while IFS= read -r LINE || [[ -n "$LINE" ]]; do
    if [[ "$LINE" =~ [^[:space:]] ]]; then
      if [[ "$LINE" =~ ^[^#]*\[ ]]; then
        SECTION="${LINE//[ \[\]]/}"
      elif [[ "$SECTION" == "$NAME" ]]; then
        SECTION_FOUND="1"
        KEY="${LINE%%=*}"   # get first word
        KEY="${KEY// /}"    # strip spaces
        # echo "$NAME: [$KEY] = $LINE"
        ARR["$KEY"]="$LINE"
      fi
    fi
  done < "$FILE"

  if [[ -z "$SECTION_FOUND" ]]; then
    echo "Error: Section [$NAME] not found in $FILE"
    exit 1
  fi
}

function find_missing_keys() {
  local -n ARR_SRC="$1"
  local -n ARR_DST="$2"
  local -n ARR_MISS="$3"
  local KEY VAL

  for KEY in "${!ARR_SRC[@]}"; do
    if [[ ! -v ARR_DST["$KEY"] ]]; then
      VAL="${ARR_SRC[${KEY}]}"
      echo "  Missing $KEY: $VAL"
      ARR_MISS["$KEY"]="$VAL"
    fi
  done

  if [[ ${#ARR_MISS[@]} -eq 0 ]]; then
    echo "  No missing keys"
  fi
}

function add_keys() {
  local NAME="$1"
  local FDST="$2"
  local FNEW="$3"
  local -n ARR_MISS="$4"

  if [[ ! -f "$FNEW" ]]; then
    $DRY cp -v "$FDST" "$FNEW"
  fi

  # Generates the lines to insert and store them in a temp file
  local LINES=$(printf "%s\n" "${ARR_MISS[@]}")
  local FTMP="${TMP:-/tmp}/_merge_toml_$NAME"
  printf "%s\n" "${ARR_MISS[@]}" > "$FTMP"

  local SECTION="\\[$NAME\\]"
  # Use sed to insert the temp file after the target pattern.
  $DRY sed -i "/$SECTION/r $FTMP" "$FNEW"
}

function process() {
  local NAME="$1"

  declare -A SRC_KEYS
  declare -A DST_KEYS
  read_keys "$NAME" "$SRC" SRC_KEYS
  read_keys "$NAME" "$DST" DST_KEYS
  echo "-- [$NAME] --"
  echo "$SRC: Section [$NAME], ${#SRC_KEYS[@]} keys"
  echo "$DST: Section [$NAME], ${#DST_KEYS[@]} keys"
  declare -A MISSING_KEYS
  find_missing_keys SRC_KEYS DST_KEYS MISSING_KEYS
  if [[ ${#MISSING_KEYS[@]} -gt 0 ]]; then
    add_keys "$NAME" "$DST" "$NEW" MISSING_KEYS
  fi
}


process "versions"
process "libraries"
process "plugins"

if [[ -f "$NEW" ]]; then
  $DRY mv -v "$DST" "${DST}.old"
  $DRY mv -v "$NEW" "$DST"
fi


if [[ -n "$DRY" ]]; then
  echo
  echo "## DRY MODE. Re-run with '$0 -f $DST' to actually modify file"
fi
echo
