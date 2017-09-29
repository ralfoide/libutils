package com.alflabs.kv;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.RSparseArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * A single {@link KeyValueProtocol} server.
 * <p/>
 * The server is designed to be unique and have multiple clients connect to it.
 * Clients either query keys to get their values or listen to updates sent by the server.
 * See {@link KeyValueProtocol} for an explanation of the exchange protocol.
 * <p/>
 * When a value changes, the server broadcasts it to all other clients.
 *
 * @see KeyValueProtocol
 */
public class KeyValueServer implements IKeyValue {
    private static final String TAG = KeyValueServer.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_VERBOSE = false;

    @NonNull private final ILogger mLogger;

    private volatile Thread mSocketThread;
    private volatile boolean mIsRunning;
    private volatile ServerSocket mServerSocket;
    private volatile int mNextSender = 0;
    private final RSparseArray<Sender> mSenders = new RSparseArray<>();
    private final KeyValueProtocol mProtocol;
    private final ExecutorService mThreadPool = Executors.newCachedThreadPool();
    private KeyValueProtocol.OnChangeListener mOnChangeListener;
    private Runnable mOnClientConnectedRunnable;

    public KeyValueServer(@NonNull ILogger logger) {
        mLogger = logger;
        mProtocol = new KeyValueProtocol(logger);
        mProtocol.setOnChangeListener((key, value) -> {
            broadcastChangeViaAllSenders(key, value);
            KeyValueProtocol.OnChangeListener l = mOnChangeListener;
            if (l != null) {
                try {
                    l.onValueChanged(key, value);
                } catch (Exception ignore) {}
            }
        });
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    // useful for unit tests
    public int getNumConnections() {
        synchronized (mSenders) {
            return mSenders.size();
        }
    }

    public void setOnClientConnected(@Null Runnable runnable) {
        mOnClientConnectedRunnable = runnable;
    }

    @Override
    public void setOnChangeListener(@Null KeyValueProtocol.OnChangeListener listener) {
        mOnChangeListener = listener;
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
     * When broadcast is true, the server is notified if there's a change. <br/>
     */
    @Override
    public void putValue(@NonNull String key, @Null String value, boolean broadcast) {
        if (mProtocol.putValue(key, value)) {
            if (broadcast) {
                broadcastChangeViaAllSenders(key, value);
            }
        }
    }

    /**
     * Starts the server on the 0.0.0.0 default address for the given port.
     * <p/>
     * Address resolution is done in a thread, which means this can be called from the UI thread.
     * <br/>
     * There is however a 5-seconds max timeout waiting for the server to bind (which most of the
     * time should be almost instantaneous.)
     * <p/>
     * Returns the socket address & port of the server, null in case of error.
     */
    public InetSocketAddress start(final int port) {
        return start(null, port);
    }

    /**
     * Starts the server on the given address for the given port.
     * <p/>
     * Caller should be careful as some methods which create an {@link InetAddress} do a DNS
     * resolution and this must not be done on the UI thread. <br/>
     * There is a 5-seconds max timeout waiting for the server to bind (which most of the
     * time should be almost instantaneous.)
     * <p/>
     * Returns the socket address & port of the server, null in case of error.
     */
    public InetSocketAddress start(@Null final InetAddress ip, final int port) {
        if (DEBUG) mLogger.d(TAG, "start | isRunning=" + mIsRunning);
        if (mIsRunning) return null;

        final CountDownLatch latch = new CountDownLatch(1);
        mIsRunning = true;
        mSocketThread = new Thread(() -> {
            if (DEBUG) mLogger.d(TAG, "socket-thread [start]");

            mServerSocket = null;
            try {
                mServerSocket = new ServerSocket();

                SocketAddress address = new InetSocketAddress(
                        ip != null ? ip : InetAddress.getByName("0.0.0.0"),
                        port);
                mServerSocket.setReuseAddress(true);
                mServerSocket.bind(address);
                latch.countDown();

                while (mIsRunning && !Thread.interrupted()) {
                    if (DEBUG) mLogger.d(TAG, "socket-thread [accept] ");
                    final Socket socket = mServerSocket.accept();
                    if (DEBUG) mLogger.d(TAG, "socket-thread [accept] " + socket);
                    mThreadPool.execute(() -> {
                        try {
                            if (DEBUG) mLogger.d(TAG, "socket-thread [pool-worker start] ");
                            processConnection(socket);
                            if (DEBUG) mLogger.d(TAG, "socket-thread [pool-worker end] ");
                        } catch (IOException e) {
                            if (DEBUG) mLogger.d(TAG, "socket-thread [pool-worker] " + e);
                        }
                    });
                }

            } catch (SocketException e) {
                // It's expected to get SocketException due to socket.close here from stop()
                if (DEBUG) mLogger.d(TAG, "socket-thread [expected] " + e);

            } catch (Throwable t) {
                if (DEBUG) mLogger.d(TAG, "socket-thread [unexpected] " + t);

            } finally {
                if (DEBUG) mLogger.d(TAG, "socket-thread [tear down]");
                if (mServerSocket != null) {
                    try {
                        mServerSocket.close();
                    } catch (IOException ignore) {}
                    mServerSocket = null;
                }
                // any connection handler is likely to terminate itself
                // quite quickly since the socket has been shutdown so
                // we'll wait shortly for them, shouldn't be blocking much.
                mThreadPool.shutdown();
                try {
                    mThreadPool.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    if (DEBUG) mLogger.d(TAG, "socket-thread [awaitTermination] " + e);
                }
            }

            if (DEBUG) mLogger.d(TAG, "socket-thread [end]");
        }, TAG + "-Thread");

        // start socket thread.
        // This will unlock the latch once the server connection is bound,
        // at which point we can get the listening socket address & port.
        mSocketThread.start();
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                // latch expired
                if (DEBUG) mLogger.d(TAG, "socket-thread [latch expired]");
                return null;
            }
        } catch (InterruptedException e) {
            // Interrupted while waiting...
            if (DEBUG) mLogger.d(TAG, "socket-thread [latch interrupted] " + e);
            return null;
        }
        InetSocketAddress address = (InetSocketAddress) mServerSocket.getLocalSocketAddress();
        if (DEBUG) mLogger.d(TAG, "socket-thread listening on " + address);
        return address;
    }

    /** Ask server to stop. Returns once the server is stopped. */
    public void stopSync() {
        if (mIsRunning) {
            stopAsync();
            try {
                if (DEBUG) mLogger.d(TAG, "stop -| join, state=" + mSocketThread.getState());
                mSocketThread.join();
                if (DEBUG) mLogger.d(TAG, "stop -| joined");
                mSocketThread = null;
            } catch (Throwable t) {
                mLogger.d(TAG, "stop -| join interrupted: " + t);
            }
        }
    }

    /** Ask server to stop. Returns immediately. */
    public void stopAsync() {
        if (DEBUG) mLogger.d(TAG, "stop -| isRunning=" + mIsRunning);
        if (mIsRunning) {
            mIsRunning = false;
            if (DEBUG) mLogger.d(TAG, "stop -| socket.close, thread.state=" + mSocketThread.getState());
            ServerSocket socket = mServerSocket;
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    if (DEBUG) mLogger.d(TAG, "stop -| socket.close=" + e);
                }
            }
        }
    }

    /**
     * Waits for incoming lines.
     * Passes any IOException from the reader to the caller, including possibly
     * the thread being interrupted.
     * @throws IOException
     */
    private void processConnection(@NonNull final Socket socket) throws IOException {
        BufferedReader in = null;
        PrintWriter out = null;
        Sender sender = null;
        int senderIndex = -1;

        try {
            // Set initial parameters: Disable nagle algorithm,
            // enable keep alive and allow infinite read timeout.
            socket.setTcpNoDelay(true); // disable nagle
            socket.setKeepAlive(true);  // try to keep alive
            socket.setSoTimeout(0 /*ms*/);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final PrintWriter out_ = out = new PrintWriter(socket.getOutputStream());
            final LinkedBlockingDeque<String> outCommands = new LinkedBlockingDeque<>();
            sender = new Sender(outCommands);

            synchronized (mSenders) {
                senderIndex = mNextSender++;
                mSenders.put(senderIndex, sender);
            }
            if (DEBUG) mLogger.d(TAG, "Added sender " + senderIndex);

            if (mOnClientConnectedRunnable != null) {
                try {
                    mOnClientConnectedRunnable.run();
                } catch (Exception ignore) {}
            }

            mThreadPool.execute(() -> {
                if (DEBUG) mLogger.d(TAG, "Writer thread started");
                while (socket.isConnected() && !socket.isClosed() && mIsRunning) {
                    try {
                        String line = outCommands.takeFirst();
                        if (line != null) {
                            if (DEBUG_VERBOSE) mLogger.d(TAG, "WRITE << " + line.trim());
                            out_.println(line);
                            out_.flush();
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (DEBUG) mLogger.d(TAG, "Writer thread ended");
            });

            sender.sendInit();

            while (socket.isConnected() && mIsRunning && !Thread.interrupted()) {
                String line = in.readLine();
                try {
                    mProtocol.processLine(sender, line);
                } catch (KeyValueProtocol.QCloseRequestException e) {
                    if (DEBUG) mLogger.d(TAG, "Q Close Request received.");
                    break;
                } catch (Exception e) {
                    if (DEBUG) mLogger.d(TAG, "Malformed line '" + line + "': " + e);
                }
            }
        } finally {
            if (senderIndex != -1) {
                synchronized (mSenders) {
                    mSenders.remove(senderIndex);
                }
                if (DEBUG) mLogger.d(TAG, "Removed sender " + senderIndex);

                if (mOnClientConnectedRunnable != null) {
                    try {
                        mOnClientConnectedRunnable.run();
                    } catch (Exception ignore) {}
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {}
            }
            if (out != null) {
                out.close();
            }
            try {
                socket.close();
            } catch (IOException ignore) {}
        }
    }

    private void broadcastChangeViaAllSenders(@NonNull String key, @Null String value) {
        if (value == null) value = "";
        synchronized (mSenders) {
            for (int n = mSenders.size() - 1; n >= 0; n--) {
                mSenders.valueAt(n).sendValue(key, value);
            }
        }
    }

    private static class Sender extends KeyValueProtocol.Sender {
        @NonNull
        private final LinkedBlockingDeque<String> mCommands;

        public Sender(@NonNull LinkedBlockingDeque<String> commands) {
            mCommands = commands;
        }

        public void sendLine(@NonNull String line) {
            mCommands.offerLast(line);
        }

        public void sendInit() {
            // server version
            sendLine("VJuniorDayModelServer:1");
        }
    }
}
