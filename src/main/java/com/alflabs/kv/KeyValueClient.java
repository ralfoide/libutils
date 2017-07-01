package com.alflabs.kv;

import android.util.Log;
import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.libutils.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyValueClient implements IConnection {
    private static final String TAG = KeyValueClient.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final boolean DEBUG_VERBOSE = true;

    public interface IListener {

        void addBandwidthTXBytes(int count);

        void addBandwidthRXBytes(int count);

        void setMessage(@Null String msg);

        void HBLatencyRequestSent();

        void HBLatencyReplyReceived();
    }

    @NonNull private final Thread mSocketThread;
    @NonNull private final IListener mListener;
    @NonNull private final KeyValueProtocol mProtocol = new KeyValueProtocol() {
        @Override
        protected void processPing(@NonNull Sender sender, @NonNull String line) {
            super.processPing(sender, line);
            onReceiveClientHeartBeat(line);
        }
    };
    private volatile long mHBValue = 1;
    private volatile boolean mIsRunning;
    private AtomicBoolean mStartSyncSuccess;
    private CountDownLatch mStartSyncLatch;

    /**
     * A dequeue (double-queue, can add/remove from head or tail) that is
     * concurrent multi-thread safe and that can be blocking.
     *
     * We add at the end using offerLast() that does not block nor throws. It would
     * only reject with false if the queue had reached its limit but we run it without
     * a limit.
     * We remove from the head using takeFirst() that blocks.
     */
    private final LinkedBlockingDeque<String> mOutCommands = new LinkedBlockingDeque<>();

    private final KeyValueProtocol.Sender mSender = new KeyValueProtocol.Sender() {
        @Override
        public void sendLine(@NonNull String line) {
            mOutCommands.offerLast(line);
        }
    };

    public KeyValueClient(@NonNull final SocketAddress address, @NonNull IListener listener) {
        mListener = listener;
        mSocketThread = new Thread(() -> {
            while (mIsRunning) {
                updateCnxMessage("Opening connection...");

                Socket socket = null;
                for (int i = 0; socket == null && mIsRunning && i < 10; i++) {
                    if (DEBUG) Log.d(TAG, "[" + i + "] Trying to connect to " + address);
                    socket = new Socket();
                    try {
                        socket.connect(address, 1000 /*ms*/);
                    } catch (IOException e) {
                        socket = null;
                        if (DEBUG) Log.d(TAG, "[" + i + "] Connect failed: " + e.toString());
                        String a = address.toString();
                        if (address instanceof InetSocketAddress) {
                            a = ((InetSocketAddress) address).getAddress().getHostAddress();
                        }
                        String d = "";
                        switch (i % 3) {
                            case 2: d += ".";
                            case 1: d += ".";
                            case 0: d += ".";
                        }
                        updateCnxMessage("Trying to connect to " + a + " " + d);
                    }
                }

                if (mStartSyncSuccess != null) {
                    mStartSyncSuccess.set(socket != null);
                }

                if (socket == null) {
                    Log.d(TAG, "Socket null [wait before retrying]");
                    if (mStartSyncLatch != null) {
                        mStartSyncLatch.countDown();
                        return;
                    }

                    try {
                        Thread.sleep(1000 /*ms*/);
                    } catch (InterruptedException e) {
                        // This should happen when stop() is called.
                        Log.d(TAG, "Socket sleep interrupted: " + e);
                    }

                } else {
                    Log.d(TAG, "Socket opened [Start read/write threads]");
                    if (mStartSyncLatch != null) {
                        mStartSyncLatch.countDown();
                    }

                    setSocketParams(socket);
                    Thread reader = readSocket(socket);
                    Thread writer = writeSocket(socket);

                    try {
                        updateCnxMessage("Reading server information...");

                        // Once we got connected, wait up to 60 seconds that the reader
                        // can get and process the init state sent by the server.
                        int version = 0;
                        for (int i = 1; i <= 600; i++) {
                            Thread.sleep(100 /*ms*/);
                             version = mProtocol.getServerVersion();
                            if (version != 0) {
                                Log.d(TAG, "Got server version in " + i*100 + " ms");
                                break;
                            }
                        }
                        Log.d(TAG, "Got server version: " + version);
                        updateCnxMessage("Connected to server v" + version);

                        // heart beat
                        while (mIsRunning && socket.isConnected() && !socket.isClosed()) {
                            Thread.sleep(1000 /*ms*/);
                            sendClientHeartBeat();
                        }

                        // Just wait for the reader/writer threads to do their work.
                        reader.join();
                        writer.join();

                    } catch (InterruptedException e) {
                        // This should happen when stop() is called.
                        Log.d(TAG, "Read/write thread interrupted: " + e);

                        if (!mIsRunning && socket.isConnected()) {
                            // This thread got interrupt because we must quit this connection.
                            // Notify the server we're closing this throttle cleanly.
                            mSender.sendCnxQuit();
                            try {
                                writer.join(250 /*ms*/);
                            } catch (InterruptedException e1) {
                                // we'll close everything below.
                            }
                        }
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            Log.d(TAG, "Socket close exception: " + e);
                        }
                        reader.interrupt();
                        writer.interrupt();
                    }

                    Log.d(TAG, "Socket lost: | isRunning=" + mIsRunning);
                }
            }

            if (DEBUG) Log.d(TAG, "end of SocketThread.");
        }, TAG + "-Thread");
    }

    public void setOnChangeListener(@Null KeyValueProtocol.OnChangeListener listener) {
        mProtocol.setOnChangeListener(listener);
    }

    @NonNull
    public IListener getListener() {
        return mListener;
    }

    /** Returns the value for the given key or null if it doesn't exist. */
    @Null
    public String getValue(@NonNull String key) {
        return mProtocol.getValue(key);
    }

    /**
     * Sets the non-value for the given key.
     * A null value removes the key if it existed.
     * When broadcast is false, the change is purely internal.
     * When broadcast is true, the server is notified if there's a change.
     */
    public void putValue(@NonNull String key, @Null String value, boolean broadcast) {
        if (mProtocol.putValue(key, value)) {
            if (broadcast) {
                if (value == null) value = "";
                mSender.sendValue(key, value);
            }
        }
    }

    /**
     * Starts asynchronously.
     * Doesn't indicate whether the connection was successful.
     * The client thread will keep trying to connect as long as possible.
     */
    public void startAsync() {
        if (DEBUG) Log.d(TAG, "start | isRunning=" + mIsRunning);
        if (!mIsRunning) {
            mIsRunning = true;
            mSocketThread.start();
        }
    }

    /**
     * Starts blocking and waits for the start to either fail or succeed.
     * In case of failure there is no retry.
     * @return True if client started and got connected.
     * @throws InterruptedException if count down latch gets interrupted whilst waiting.
     */
    public boolean startSync() throws InterruptedException {
        if (!mIsRunning) {
            mStartSyncSuccess = new AtomicBoolean(false);
            mStartSyncLatch = new CountDownLatch(1);
            startAsync();
            mStartSyncLatch.await();
        }
        return mStartSyncSuccess != null && mStartSyncSuccess.get();
    }

    public void stopAsync() {
        if (DEBUG) Log.d(TAG, "stop -| isRunning=" + mIsRunning);
        if (mIsRunning) {
            mIsRunning = false;

            mSender.sendCnxQuit();
            try {
                Thread.sleep(250 /*ms*/);
            } catch (InterruptedException ignore) {}

            mSocketThread.interrupt();
        }
    }

    public void stopSync() {
        if (mIsRunning) {
            stopAsync();
            try {
                mSocketThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "stop -| join interrupted: " + e);
            }
        }
    }

    private void setSocketParams(@NonNull Socket socket) {
        // Set initial parameters: Disable nagle algorithm,
        // enable keep alive and allow infinite read timeout.
        try {
            socket.setTcpNoDelay(true);     // disable nagle
            socket.setKeepAlive(true);      // try to keep alive
            socket.setSoTimeout(0 /*ms*/);  // infinite *read* timeout
        } catch (SocketException e) {
            Log.d(TAG, "socket set params failed: " + e);
        }
    }

    private Thread writeSocket(@NonNull final Socket socket) {
        Thread t = new Thread(() -> {
            if (DEBUG) Log.d(TAG, "start of writeSocket thread.");
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                while (socket.isConnected() &&
                        !socket.isOutputShutdown() &&
                        mIsRunning &&
                        !Thread.interrupted()) {
                    String line = getNextWriterLine();
                    if (DEBUG_VERBOSE) Log.d(TAG, "WRITE << " + line.trim());
                    out.println(line);
                    out.flush();
                    mListener.addBandwidthTXBytes(line.length() + 1);
                }
            } catch (IOException e) {
                if (DEBUG) Log.d(TAG, "WRITE failed: " + e);
            } catch (InterruptedException e) {
                if (DEBUG) Log.d(TAG, "getNextWriterLine interrupted: " + e);
            }
            if (DEBUG) Log.d(TAG, "end of writeSocket thread.");
        }, TAG + "-Writer");
        t.start();
        return t;
    }

    /**
     * Returns the next line to write to the output stream/socket.
     * This call will block till there's something to return or
     * when the thread is interrupted.
     */
    @NonNull
    protected String getNextWriterLine() throws InterruptedException {
        return mOutCommands.takeFirst();
    }

    private Thread readSocket(@NonNull final Socket socket) {
        Thread t = new Thread(() -> {
            if (DEBUG) Log.d(TAG, "start of readSocket thread.");
            // We've set SO_TIMEOUT to zero meaning reads will block forever.
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (socket.isConnected() &&
                        !socket.isInputShutdown() &&
                        mIsRunning &&
                        !Thread.interrupted()) {
                    try {
                        String line = in.readLine();
                        if (line != null) {  // null when readLine is interrupted
                            mListener.addBandwidthRXBytes(line.length() + 1);
                            if (DEBUG_VERBOSE) Log.d(TAG, "READ >> " + line.trim());
                            mProtocol.processLine(mSender, line);
                        }
                    } catch (SocketTimeoutException expected) {
                        // We're blocking on input, this should never happen
                        if (DEBUG_VERBOSE) Log.d(TAG, "READ expected: " + expected);
                    }
                }
            } catch (IOException e) {
                if (DEBUG) Log.d(TAG, "READ failed: " + e);
            }
            if (DEBUG) Log.d(TAG, "end of readSocket thread.");
        }, TAG + "-Reader");
        t.start();
        return t;
    }

    private void updateCnxMessage(@Null String msg) {
        mListener.setMessage(msg);
    }

    private void sendClientHeartBeat() {
        synchronized (mListener) {
            if (mHBValue > 0) {
                if (DEBUG_VERBOSE) Log.d(TAG, "HB SEND value " + mHBValue);
                mSender.sendPing(Long.toString(mHBValue));
                mListener.HBLatencyRequestSent();
                mHBValue = -mHBValue; // make it negative while waiting for an answer
            }
        }
    }

    private void onReceiveClientHeartBeat(String line) {
        synchronized (mListener) {
            if (mHBValue < 0) {
                long value = -1 * mHBValue;
                String expected = "PR" + value;
                if (DEBUG_VERBOSE) Log.d(TAG, "HB RECEIVE, expected '" + expected + "', got '" + line + "'");
                if (expected.equals(line)) {
                    mListener.HBLatencyReplyReceived();
                    mHBValue = 1 + value;
                    mListener.setMessage(null);
                }
            }
        }
    }

    public void requestAllKeys() {
        mSender.requestAllKeys();
    }

    public void requestKey(@NonNull String key) {
        mSender.requestKey(key);
    }
}
