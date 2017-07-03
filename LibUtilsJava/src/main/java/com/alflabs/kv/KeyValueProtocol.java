package com.alflabs.kv;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.utils.ILogger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Protocol is text & line-oriented.
 * All communication should be UTF-8.
 * Server > Client.
 * The server holds key-value pairs. Client can request values and notifies of updates.
 * Names and values are simple strings.
 * Names cannot contain the colon (:) character.
 * Values end with any EOL.
 * Names and values are trimmed so any whitespace at the beginning or end is removed.
 *
 * - Server init: "ModelServer:version" with version int >= 1.
 * - Server writes value: "Wname:value"
 * - Client reads all values: "R*" ==> server replies with all known values, one per line.
 * - Client reads value: "Rname" ==> server replies with single value.
 * - Client writes value: "Wname:value"
 * - Client ping for keep-alives: "PSstring" (ping send) ==> server replies with PR (ping reply) + rest of the line.
 * - Client close: "Q" ==> server closes this client connection
 */
public class KeyValueProtocol {
    private static final String TAG = KeyValueProtocol.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_VERBOSE = false;

    /**
     * Synchronized value map.
     * Simple operations are synchronized by the collection on the map itself.
     * Non-"atomic" operations such as iterators much synchronize on the map itself.
     * The map is a tree map so iterator is stable on the key ordering.
     */
    private final Map<String, String> mValues = Collections.synchronizedMap(new TreeMap<String, String>());
    @NonNull
    private final ILogger mLogger;
    @Null private OnChangeListener mOnChangeListener;
    private int mServerVersion = 0;

    public interface OnChangeListener {
        public void onValueChanged(@NonNull String key, @Null String value);
    }

    public KeyValueProtocol(@NonNull ILogger logger) {
        mLogger = logger;
    }

    public void setOnChangeListener(@Null OnChangeListener listener) {
        mOnChangeListener = listener;
    }

    /** Returns the value for the given key or null if it doesn't exist. */
    @Null
    public String getValue(@NonNull String key) {
        return mValues.get(key);
    }

    /**
     * Sets the non-value for the given key. A null value removes the key if it existed.
     * Returns true if the value has been changed.
     */
    public boolean putValue(@NonNull String key, @Null String value) {
        synchronized (mValues) {
            String existing = mValues.get(key);
            if (value == null) {
                mValues.remove(key);
                return existing != null && !existing.isEmpty();
            } else {
                mValues.put(key, value);
                return !value.equals(existing);
            }
        }
    }

    public int getServerVersion() {
        return mServerVersion;
    }

    static class QCloseRequestException extends IOException {
    }

    void processLine(@NonNull Sender sender, @Null String line) throws QCloseRequestException {
        if (line == null) return;
        line = line.trim();
        if (line.isEmpty()) return;
        if (DEBUG_VERBOSE) mLogger.d(TAG, "Input: " + line);

        char prefix = line.charAt(0);
        switch (prefix) {
            case 'V': {
                // Server version
                String key = line.substring(1);
                String fields[] = key.split(":", 2);
                if (DEBUG_VERBOSE) mLogger.d(TAG, "Process V: " + Arrays.toString(fields));
                if (fields.length < 2) break;
                key = fields[0].trim();
                if ("JuniorDayModelServer".equals(key)) {
                    try {
                        mServerVersion = Integer.parseInt(fields[1].trim());
                    } catch (NumberFormatException ignore) {}
                }
                break;
            }

            case 'R':
                // Read value
                processRead(sender, line);
                break;

            case 'W':
                // Write (change) value
                processWrite(line);
                break;

            case 'P':
                processPing(sender, line);
                break;

            case 'Q':
                processQuit();
                break;
        }
    }

    protected void processQuit() throws QCloseRequestException {
        if (DEBUG_VERBOSE) mLogger.d(TAG, "Process Q");
        throw new QCloseRequestException();
    }

    // Ping Send (PS prefix) ... we reply with Ping Reply (PR prefix) + value
    // P followed by any other prefix than S is ignored.
    protected void processPing(@NonNull Sender sender, @NonNull String line) {
        if (DEBUG_VERBOSE) mLogger.d(TAG, "Process P: " + line);
        if (line.length() > 2) {
            char prefix = line.charAt(1);
            if (prefix == 'S') {
                sender.sendLine("PR" + line.substring(2));
            }
        }
    }

    protected void processRead(@NonNull Sender sender, @NonNull String line) {
        String key = line.substring(1).trim();
        if (DEBUG_VERBOSE) mLogger.d(TAG, "Process R: " + key);
        if (key.isEmpty()) return;  // ignore empty names
        if ("*".equals(key)) {
            synchronized (mValues) {
                for (Map.Entry<String, String> entry : mValues.entrySet()) {
                    key = entry.getKey();
                    String value = entry.getValue();
                    if (value == null) {
                        value = "";
                    }
                    sender.sendValue(key, value);
                }
            }
        } else {
            String value = mValues.get(key);
            if (value == null) {
                value = "";
            }
            sender.sendValue(key, value);
        }
    }

    protected void processWrite(@NonNull String line) {
        String key = line.substring(1);
        String fields[] = key.split(":", 2);
        if (DEBUG_VERBOSE) mLogger.d(TAG, "Process W: " + Arrays.toString(fields));
        if (fields.length < 2) return;
        key = fields[0].trim();
        if (key.isEmpty()) return;  // ignore empty names
        String value = fields[1].trim();
        boolean changed;
        synchronized (mValues) {
            String existing = mValues.get(key);
            if (existing == null) {
                existing = "";
            }
            changed = !existing.equals(value);
            if (changed) {
                mValues.put(key, value);
            }
        }
        if (changed) {
            OnChangeListener l = mOnChangeListener;
            if (l != null) {
                try {
                    l.onValueChanged(key, value);
                } catch (Throwable t) {
                    if (DEBUG) mLogger.d(TAG, "OnChangeListener failed", t);
                }
            }
        }
    }


    abstract static class Sender {
        public abstract void sendLine(@NonNull String line);

        public void sendValue(@NonNull String key, @NonNull String value) {
            sendLine("W" + key + ":" + value);
        }

        public void sendPing(@NonNull String value) {
            // Ping Send ... server will reply with Ping Reply + value
            sendLine("PS" + value);
        }

        public void sendCnxQuit() {
            sendLine("Q");
        }

        public void requestAllKeys() {
            sendLine("R*");
        }

        public void requestKey(@NonNull String key) {
            sendLine("R" + key);
        }
    }
}
