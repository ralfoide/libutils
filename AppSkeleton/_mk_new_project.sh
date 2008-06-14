#!/bin/bash

#
# $Id: _mk_new_project.sh,v 1.3 2005/04/27 01:12:01 ralf Exp $
#

OLDNAME=AppSkeleton
OLDNS=AppSkeletonNs
OLDAPP=ClientApp
OLDLIB=ClientLib

PNAME="$1"

NSNAME="$2"
if [ x"$NSNAME" == x"" ]; then NSNAME="$PNAME"; fi

APPNAME="$3"
if [ x"$APPNAME" == x"" ]; then APPNAME="${PNAME}App"; fi

LIBNAME="$4"
if [ x"$LIBNAME" == x"" ]; then LIBNAME="${PNAME}Lib"; fi


function usage()
{
	echo
	echo "Usage: $0 project-name [namespace [client-app [client-lib]]]"
	echo
	echo "Defaults:"
	echo "   Namespace  = <project-name>"
	echo "   Client App = <project-name>App"
	echo "   Client Lib = <project-name>Lib"
	echo
	echo "Important: Run this from the $OLDNAME directory. "
	echo "New project will be created as ../<project-name>."
	echo
	exit 1
}

if [ x"$PNAME" == x"" ]
then
	usage
fi

#--------------------------------------

# Info

echo
echo "New project $PNAME will be created with:"
echo "   Namespace  = Alfray.$NSNAME"
echo "   Client App = Alfray.$NSNAME.$APPNAME"
echo "   Client Lib = Alfray.$NSNAME.$LIBNAME"
echo


#--------------------------------------

# remove dest dir

DEST="../$PNAME"

if [ -e "$DEST" ]
then

	CONFIRM=""
	while [ "$CONFIRM" != "Y" ] && [ "$CONFIRM" != "N" ]
	do
		read -p "Do you want to remove existing $DEST? [y/N] " CONFIRM
	
		#Set default to No
		CONFIRM=${CONFIRM:="N"}
		# Get only first letter: Y or y
		CONFIRM=${CONFIRM:0:1}
		if [ "$CONFIRM" == "y" ]; then CONFIRM=Y; fi
		if [ "$CONFIRM" == "n" ]; then CONFIRM=N; fi
	done

	if [ "$CONFIRM" == "Y" ]
	then
		echo "Removing old $PNAME..."
		rm -rf "$DEST"
	else
		echo "Aborting."
		exit 0
	fi
fi

#--------------------------------------

# copy the whole tree

mkdir -p "$DEST"

echo "Copying new $PNAME..."
cp -r * "$DEST/."

#--------------------------------------

# remove unwanted stuff

cd "$DEST"

for i in _mk_new_project.sh CVS bin obj "*.user" "*.suo"
do
	echo "Removing */$i"
	find .  -depth -name "$i" -exec rm -rf {} \;
done

#--------------------------------------

# rename files

echo "Renaming files..."

function rename_files()
{
	OLD="$1"
	shift
	NEW="$1"
	shift
	for i in $*
	do
		A=`basename "$i"`
		B=`dirname "$i"`
		C="$B/$A"
		D=${A/$OLD/$NEW}
		E="$B/$D"
		mv -f "$C" "$E"
	done
}

rename_files $OLDNAME $PNAME   `find . -depth -name "*$OLDNAME*" -print0 | xargs -0`
rename_files $OLDAPP  $APPNAME `find . -depth -name "*$OLDAPP*"  -print0 | xargs -0`
rename_files $OLDLIB  $LIBNAME `find . -depth -name "*$OLDLIB*"  -print0 | xargs -0`


#--------------------------------------

# replace inner file content

echo "Editing names in files..."

LIST=`find . -depth -name "*" -print0 | xargs -0`

for A in $LIST
do
	if grep -q -s -E "($OLDNS|$OLDNAME|$OLDAPP|$OLDLIB)" "$A"
	then
		echo "  Editing $A"
		B="$A.temp.txt"
		sed -e "s/\([^A-Za-z0-9_]\|^\)$OLDNS\([^A-Za-z0-9_]\|$\)/\1$NSNAME\2/g"  \
			-e "s/\([^A-Za-z0-9_]\|^\)$OLDNAME\([^A-Za-z0-9_]\|$\)/\1$PNAME\2/g" \
			-e "s/\([^A-Za-z0-9_]\|^\)$OLDAPP\([^A-Za-z0-9_]\|$\)/\1$APPNAME\2/g" \
			-e "s/\([^A-Za-z0-9_]\|^\)$OLDLIB\([^A-Za-z0-9_]\|$\)/\1$LIBNAME\2/g" \
			"$A" > "$B"
		if grep -q -s -E "($OLDNS|$OLDNAME|$OLDAPP|$OLDLIB)" "$B"
		then
			C="${B}2"
			mv -f "$B" "$C"
			sed -e "s/\([^A-Za-z0-9_]\|^\)$OLDNS\([^A-Za-z0-9_]\|$\)/\1$NSNAME\2/g"  \
				-e "s/\([^A-Za-z0-9_]\|^\)$OLDNAME\([^A-Za-z0-9_]\|$\)/\1$PNAME\2/g" \
				-e "s/\([^A-Za-z0-9_]\|^\)$OLDAPP\([^A-Za-z0-9_]\|$\)/\1$APPNAME\2/g" \
				-e "s/\([^A-Za-z0-9_]\|^\)$OLDLIB\([^A-Za-z0-9_]\|$\)/\1$LIBNAME\2/g" \
				"$C" > "$B"
			rm -f "$C"
		fi
		if [ -f "$B" ]
		then
			mv -f "$B" "$A"
		fi
	fi
done

#
# $Log: _mk_new_project.sh,v $
# Revision 1.3  2005/04/27 01:12:01  ralf
# Updated Utils with files from Xeres
#
# Revision 1.2  2005/02/18 23:21:52  ralf
# Creating both an App and a Class Lib
#
#