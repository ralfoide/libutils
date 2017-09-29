package com.alflabs.kv;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.utils.ILogger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Protocol is text & line-oriented. <br/>
 * <pre>
 * All communication should be UTF-8.
 * Server > Client.
 * The server holds key-value pairs. Client can request values and notifies of updates.
 * Names and values are simple strings.
 * Names cannot contain the colon (:) character.
 * Values end with any EOL.
 * Names and values are trimmed so any whitespace at the beginning or end is removed.
 * There are no pure null values. Null values, empty strings and missing keys are mostly equivalent.
 * </pre><pre>
 * - Server init: "ModelServer:version" with version int >= 1.
 * - Server writes value: "Wname:value".
 * - Client reads all values: "R*" ==> server replies with all known values, one per line.
 * - Client reads value: "Rname" ==> server replies with single value.
 * - Client writes value: "Wname:value".
 * - Client ping for keep-alives: "PSstring" (ping send) ==> server replies with PR (ping reply) + rest of the line.
 * - Client close: "Q" ==> server closes this client connection.
 * </pre>
 * The protocol object actually holds the current state of all key-values in a map. <br/>
 * It provides the logic to decode command lines received from the network. <br/>
 * The {@link Sender} provides the logic to encode command lines to be sent on the network.
 * Neither the {@link KeyValueProtocol} nor {@link Sender} do any network operations, this is
 * left to the actual client and server implementations. <br/>
 * Since the protocol is symetrical, both the clients and the server use the same protocol encoding
 * and decoding on both sides to communicate. <br/>
 * When the protocol receives a write command, it stores the new value in its key-value map
 * and notifies the {@link OnChangeListener} if the value has changed.
 */
public class KeyValueProtocol {
    private static final String TAG = KeyValueProtocol.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_VERBOSE = false;

    /**
     * Synchronized value map. <br/>
     * Simple operations are synchronized by the collection on the map itself.
     * Non-"atomic" operations such as iterators much synchronize on the map itself.
     * The map is a tree map so iterator is stable on the key ordering.
     */
    private final Map<String, String> mValues = Collections.synchronizedMap(new TreeMap<String, String>());
    @NonNull private final ILogger mLogger;
    @Null private OnChangeListener mOnChangeListener;
    private int mServerVersion = 0;

    /** Listener used to notify users when a key or value has changed. */
    public interface OnChangeListener {
        /** Notifies the listener that the value for the given key has changed. */
        void onValueChanged(@NonNull String key, @Null String value);
    }

    /** Creates a new {@link KeyValueProtocol} with the specified logger. */
    public KeyValueProtocol(@NonNull ILogger logger) {
        mLogger = logger;
    }

    /** Sets the unique {@link OnChangeListener}, notified when a write command changed a value. */
    public void setOnChangeListener(@Null OnChangeListener listener) {
        mOnChangeListener = listener;
    }

    /** Returns all the keys available. */
    @NonNull
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(mValues.keySet());
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

    /** Protocol command was received that requested the client/server connection to be closed. */
    static class QCloseRequestException extends IOException {
    }

    /**
     * Decodes a command line received from the network. <br/>
     * This is the counterpart to {@link Sender}.
     *
     * @param sender Sender to use for replies.
     * @param line The line received from the network, as-is.
     * @throws QCloseRequestException
     */
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

    // Ping Send (PS prefix) ... we reply with Ping Reply (PR prefix) + value.
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

    /**
     * A "sender" is an utility class that implements the various commands that can be
     * exchanged between clients and servers. It formats the commands appropriately and
     * generates the text line to be sent on the network.
     * <p/>
     * This is an abstract class. Derived implementations perform the actual network
     * operation needed to send the formatted command line.
     */
    abstract static class Sender {
        /** Implemented by actual implementations to send the text line on a network socket. */
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
