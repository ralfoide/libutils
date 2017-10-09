package com.alflabs.kv;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.rx.IStream;
import com.alflabs.utils.ILogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A single {@link KeyValueProtocol} client.
 * <p/>
 * Many clients connect to the same server.
 * Clients either query keys to get their values or listen to updates sent by the server.
 * See {@link KeyValueProtocol} for an explanation of the exchange protocol.
 * <p/>
 * When a value changes, the server broadcasts it to all other clients.
 *
 * @see KeyValueProtocol
 */
public class KeyValueClient implements IConnection, IKeyValue {
    private static final String TAG = KeyValueClient.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_VERBOSE = true;

    public interface IStatsListener {
        void addBandwidthTXBytes(int count);

        void addBandwidthRXBytes(int count);

        void setMessage(@Null String msg);

        void HBLatencyRequestSent();

        void HBLatencyReplyReceived();
    }

    @NonNull private final Thread mSocketThread;
    @NonNull private final ILogger mLogger;
    @NonNull private final IStatsListener mStatsListener;
    @NonNull private final KeyValueProtocol mProtocol;
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

    public KeyValueClient(
            @NonNull ILogger logger,
            @NonNull final SocketAddress address,
            @NonNull IStatsListener statsListener) {
        mLogger = logger;
        mStatsListener = statsListener;

        mProtocol = new KeyValueProtocol(mLogger) {
            @Override
            protected void processPing(@NonNull Sender sender, @NonNull String line) {
                super.processPing(sender, line);
                onReceiveClientHeartBeat(line);
            }
        };

        mSocketThread = new Thread(() -> serverOnThread(address), TAG + "-Thread");
    }

    private void serverOnThread(@NonNull SocketAddress address) {
        while (mIsRunning) {
            updateCnxMessage("Opening connection...");

            Socket socket = createSocket(address);

            if (mStartSyncSuccess != null) {
                mStartSyncSuccess.set(socket != null);
            }

            if (socket == null) {
                mLogger.d(TAG, "Socket null [wait before retrying]");
                if (mStartSyncLatch != null) {
                    mStartSyncLatch.countDown();
                    return;
                }

                try {
                    Thread.sleep(1000 /*ms*/);
                } catch (InterruptedException e) {
                    // This should happen when stop() is called.
                    mLogger.d(TAG, "Socket sleep interrupted: " + e);
                }

            } else {
                socketConnected(socket);
            }
        }

        if (DEBUG) {
            mLogger.d(TAG, "end of SocketThread.");
        }
    }

    private void socketConnected(Socket socket) {
        mLogger.d(TAG, "Socket opened [Start read/write threads]");
        if (mStartSyncLatch != null) {
            mStartSyncLatch.countDown();
        }

        setSocketParams(socket);
        CountDownLatch readWriteLatch = new CountDownLatch(1);
        Thread reader = readSocket(socket, readWriteLatch);
        Thread writer = writeSocket(socket, readWriteLatch);

        try {
            initConnection();

            // Just wait for the reader/writer threads to do their work.
            // Send the heart beat as long as the reader/writer threads have not finished.
            while (mIsRunning && !socket.isClosed() && !readWriteLatch.await(1, TimeUnit.SECONDS)) {
                sendClientHeartBeat();
            }

        } catch (InterruptedException e) {
            // This should happen when stop() is called.
            mLogger.d(TAG, "Read/write thread interrupted: " + e);

            handleQuit(writer);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                mLogger.d(TAG, "Socket close exception: " + e);
            }
            reader.interrupt();
            writer.interrupt();
        }

        mLogger.d(TAG, "Socket lost: | isRunning=" + mIsRunning);
    }

    @Null
    private Socket createSocket(@NonNull SocketAddress address) {
        Socket socket = null;
        for (int i = 0; socket == null && mIsRunning && i < 10; i++) {
            if (DEBUG) {
                mLogger.d(TAG, "[" + i + "] Trying to connect to " + address);
            }
            socket = new Socket();
            try {
                socket.connect(address, 1000 /*ms*/);
            } catch (IOException e) {
                socket = null;
                if (DEBUG) {
                    mLogger.d(TAG, "[" + i + "] Connect failed: " + e.toString());
                }
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
        return socket;
    }

    private void initConnection() throws InterruptedException {
        updateCnxMessage("Reading server information...");

        // Once we got connected, wait up to 60 seconds that the reader
        // can get and process the init state sent by the server.
        int version = 0;
        for (int i = 1; i <= 600; i++) {
            Thread.sleep(100 /*ms*/);
            version = mProtocol.getServerVersion();
            if (version != 0) {
                mLogger.d(TAG, "Got server version in " + i*100 + " ms");
                break;
            }
        }
        mLogger.d(TAG, "Got server version: " + version);
        updateCnxMessage("Connected to server v" + version);
    }

    private void handleQuit(Thread writer) {
        if (!mIsRunning) {
            // This thread got interrupt because we must quit this connection.
            // Notify the server we're closing this throttle cleanly.
            mSender.sendCnxQuit();
            try {
                writer.join(250 /*ms*/);
            } catch (InterruptedException e1) {
                // we'll close everything below.
            }
        }
    }

    @NonNull
    public IStream<String> getChangedStream() {
        return mProtocol.getChangedStream();
    }

    @NonNull
    public IStatsListener getStatsListener() {
        return mStatsListener;
    }

    /** Returns all the keys available. */
    @Override
    @NonNull
    public Set<String> getKeys() {
        return mProtocol.getKeys();
    }

    /** Returns the value for the given key or null if it doesn't exist. */
    @Override
    @Null
    public String getValue(@NonNull String key) {
        return mProtocol.getValue(key);
    }

    /**
     * Sets the non-value for the given key. <br/>
     * A null value removes the key if it existed. <br/>
     * When broadcast is false, the change is purely internal. <br/>
     * When broadcast is true, the server is notified if there's a change.
     */
    @Override
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
        if (DEBUG) {
            mLogger.d(TAG, "start | isRunning=" + mIsRunning);
        }
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
        if (DEBUG) {
            mLogger.d(TAG, "stop -| isRunning=" + mIsRunning);
        }
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
                mLogger.d(TAG, "stop -| join interrupted: " + e);
            }
        }
    }

    public void join() throws InterruptedException {
        if (mIsRunning) {
            mSocketThread.join();
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
            mLogger.d(TAG, "socket set params failed: " + e);
        }
    }

    private Thread writeSocket(@NonNull Socket socket, @NonNull CountDownLatch readWriteLatch) {
        Thread t = new Thread(() -> {
            if (DEBUG) {
                mLogger.d(TAG, "start of writeSocket thread.");
            }
            try {
                writeLoop(socket);
            } catch (IOException e) {
                if (DEBUG) {
                    mLogger.d(TAG, "WRITE failed: " + e);
                }
            } catch (InterruptedException e) {
                if (DEBUG) {
                    mLogger.d(TAG, "writeLoop interrupted: " + e);
                }
            }
            if (DEBUG) {
                mLogger.d(TAG, "end of writeSocket thread.");
            }
            readWriteLatch.countDown();
        }, TAG + "-Writer");
        t.start();
        return t;
    }

    private void writeLoop(@NonNull Socket socket) throws IOException, InterruptedException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        while (mIsRunning &&
                !socket.isOutputShutdown() &&
                !Thread.interrupted()) {
            String line = getNextWriterLine();
            if (line == null) {
                if (DEBUG_VERBOSE) mLogger.d(TAG, "WRITE NULL");
                continue;
            }
            if (DEBUG_VERBOSE) mLogger.d(TAG, "WRITE >> " + line.trim());
            out.println(line);
            out.flush();
            mStatsListener.addBandwidthTXBytes(line.length() + 1);
        }
    }

    /**
     * Returns the next line to write to the output stream/socket.
     * <p/>
     * This call will partially block:
     * - After a timeout, null is returned to indicate the timeout expired.
     * - It can throw InterruptedException.
     * - Otherwise it returns a non-null value immediately when available.
     */
    @Null
    private String getNextWriterLine() throws InterruptedException {
        return mOutCommands.pollFirst(1, TimeUnit.SECONDS);
    }

    private Thread readSocket(@NonNull Socket socket, @NonNull CountDownLatch readWriteLatch) {
        Thread t = new Thread(() -> {
            if (DEBUG) {
                mLogger.d(TAG, "start of readSocket thread.");
            }
            try {
                // We've set SO_TIMEOUT to zero meaning reads will block forever.
                readLoop(socket);
            } catch (IOException e) {
                if (DEBUG) {
                    mLogger.d(TAG, "READ failed: " + e);
                }
            }
            if (DEBUG) {
                mLogger.d(TAG, "end of readSocket thread.");
            }
            readWriteLatch.countDown();
        }, TAG + "-Reader");
        t.start();
        return t;
    }

    private void readLoop(@NonNull Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while (mIsRunning &&
                !socket.isInputShutdown() &&
                !Thread.interrupted()) {
            try {
                // We've set SO_TIMEOUT to zero meaning reads will block forever.
                String line = in.readLine();
                if (line == null) {
                    // readLine returns "null if the end of the stream has been reached"
                    // or in this case when the connection is gone or it has been interrupted.
                    if (DEBUG) mLogger.d(TAG, "readLoop end");
                    return;
                }
                mStatsListener.addBandwidthRXBytes(line.length() + 1);
                if (DEBUG_VERBOSE) mLogger.d(TAG, "READ << " + line.trim());
                mProtocol.processLine(mSender, line);
            } catch (SocketTimeoutException e) {
                // We're blocking on input, this should never happen
                if (DEBUG_VERBOSE) mLogger.d(TAG, "READ SO Timeout Exception: " + e);
            }
        }
    }

    private void updateCnxMessage(@Null String msg) {
        mStatsListener.setMessage(msg);
    }

    private void sendClientHeartBeat() {
        synchronized (mStatsListener) {
            if (mHBValue > 0) {
                if (DEBUG_VERBOSE) mLogger.d(TAG, "HB SEND value " + mHBValue);
                mSender.sendPing(Long.toString(mHBValue));
                mStatsListener.HBLatencyRequestSent();
                mHBValue = -mHBValue; // make it negative while waiting for an answer
            }
        }
    }

    private void onReceiveClientHeartBeat(String line) {
        synchronized (mStatsListener) {
            if (mHBValue < 0) {
                long value = -1 * mHBValue;
                String expected = "PR" + value;
                if (DEBUG_VERBOSE) mLogger.d(TAG, "HB RECEIVE, expected '" + expected + "', got '" + line + "'");
                if (expected.equals(line)) {
                    mStatsListener.HBLatencyReplyReceived();
                    mHBValue = 1 + value;
                    mStatsListener.setMessage(null);
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
