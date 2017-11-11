/*
 * Project: Lib Utils
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.kv;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStream;
import com.alflabs.rx.Publishers;
import com.alflabs.rx.Schedulers;
import com.alflabs.rx.Streams;
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
 * The server holds key-value pairs. Clients can request values and be notified of updates.
 * Names and values are simple strings.
 * Names cannot contain the colon (:) character.
 * Values end with any EOL.
 * Names and values are trimmed so any whitespace at the beginning or end is removed.
 * There are no pure null values. Null values, empty strings and missing keys are mostly equivalent.
 * </pre><pre>
 * - Server init: "ModelServer:version" with version int >= 1.
 * - Server writes value: "Wname:value".
 * - Client reads all values: "R*" ==> server replies with "Wname:value" for all known values, one per line.
 * - Client reads value: "Rname" ==> server replies with "Wname:value" for a single value.
 * - Client writes value: "Wname:value".
 * - Client ping for keep-alives: "PSstring" (ping send) ==> server replies with PR (ping reply) + rest of the line.
 * - Client close: "Q" ==> server closes this client connection.
 * </pre>
 * The protocol object actually holds the current state of all key-values in a map. <br/>
 * It provides the logic to decode command lines received from the network. <br/>
 * The {@link Sender} provides the logic to encode command lines to be sent on the network.
 * Neither the {@link KeyValueProtocol} nor {@link Sender} do any network operations, this is
 * left to the actual client and server implementations.
 * <p/>
 * Since the protocol is symetrical, both the clients and the server use the same protocol encoding
 * and decoding on both sides to communicate. The notion of "read" and "write" in the protocol description
 * above is from the <em>sender</em>'s point of view, which can be either a client or the server: when the
 * program using the library "puts" a new value, it gets "written" (sent) via the network connection:
 * the server writes to the N clients it is connected to, whereas a client writes to the one server it is connected to.
 * <p/>
 * Write scenario: <br/>
 * Client A calls {@link #putValue} on the KVClient.
 * KVClient A sends a "Write" command to its server to update the server store.
 * The server then sends a "Write" command to all its connected clients to notify them that the value has changed.
 * This does include client A too, the server does not try to guess whether clients need the update or not.
 * Clients can subscribe to {@link #getChangedStream} to be notified when that value been updated (see below).
 * <p/>
 * Read scenario: <br/>
 * Client A calls {@link #getValue} on the KVClient. The client simply returns whatever is in the store's data map,
 * which will be empty at first. This is by design -- {@link #getValue} is "cheap" and does not generate a network
 * request, nor does it try to guess whether the value is up to date or even known.
 * The {@link KeyValueClient} offers 2 different calls that have network impact: {@link KeyValueClient#requestKey}
 * and {@link KeyValueClient#requestAllKeys}. These work asynchronously -- they trigger the server to send the data
 * using a "Write" reply. The difference with the write/update scenario above is that the reply is sent just to that
 * one client that issues the Read request, not to all connected clients.
 * Clients can subscribe to {@link #getChangedStream} to be notified when that reply comes in (see below).
 * <p/>
 * The {@link #getChangedStream} is used to notified client when a value been updated and <em>changed</em>
 * in the internal data store -- changed means the value is either seen for the first time or different.
 * The stream now only provides the key that has changed. It used to be a pair of (key, value) but that meant
 * temporary objects were created even for clients who did not need that data. Most clients will filter on
 * the key name and if relevant they can use the {@link #getValue(String)} method to retrieve the new value.
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
    private int mServerVersion = 0;

    private final IStream<String> mChangedStream = Streams.stream();
    private final IPublisher<String> mChangedPublisher = Publishers.publisher();

    /** Creates a new {@link KeyValueProtocol} with the specified logger. */
    public KeyValueProtocol(@NonNull ILogger logger) {
        mLogger = logger;
        mChangedStream
                .on(Schedulers.sync())
                .publishWith(mChangedPublisher);
    }

    /**
     *  Retrieves the stream notified when a write command updated a key and its value has changed.
     *  The published string is the name of the key that changed. It is guaranteed to be non-null.
     *  Clients can call {@link #getValue(String)} with that key if they are interested in the new value.
     */
    @NonNull
    public IStream<String> getChangedStream() {
        return mChangedStream;
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
            changed = existing == null || !existing.equals(value);
            if (changed) {
                mValues.put(key, value);
            }
        }
        if (changed) {
            try {
                mChangedPublisher.publish(key);
            } catch (Exception e) {
                mLogger.d(TAG, "Exception during publish(" + key + "): " + e);
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
