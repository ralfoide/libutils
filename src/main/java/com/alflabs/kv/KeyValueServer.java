package com.alflabs.kv;

import android.util.Log;
import android.util.SparseArray;
import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.libutils.BuildConfig;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

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
 * - Client ping for keep-alives: "PSstring" ==> server replies with PR + rest of line.
 * - Client close: "Q" ==> server closes this client connection
 */
public class KeyValueServer {
    private static final String TAG = KeyValueServer.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final boolean DEBUG_VERBOSE = false;

    private volatile Thread mSocketThread;
    private volatile boolean mIsRunning;
    private volatile ServerSocket mServerSocket;
    private volatile int mNextSender = 0;
    private final SparseArray<Sender> mSenders = new SparseArray<>();
    private final KeyValueProtocol mProtocol = new KeyValueProtocol();
    private final ExecutorService mThreadPool = Executors.newCachedThreadPool();
    private KeyValueProtocol.OnChangeListener mOnChangeListener;
    private Runnable mOnClientConnectedRunnable;

    public KeyValueServer() {
        mProtocol.setOnChangeListener(new KeyValueProtocol.OnChangeListener() {
            @Override
            public void onValueChanged(@NonNull String key, @Null String value) {
                broadcastChangeToAllSenders(key, value);
                KeyValueProtocol.OnChangeListener l = mOnChangeListener;
                if (l != null) {
                    try {
                        l.onValueChanged(key, value);
                    } catch (Exception ignore) {}
                }
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

    public void setOnChangeListener(@Null KeyValueProtocol.OnChangeListener listener) {
        mOnChangeListener = listener;
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
                broadcastChangeToAllSenders(key, value);
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
        if (DEBUG) Log.d(TAG, "start | isRunning=" + mIsRunning);
        if (mIsRunning) return null;

        final CountDownLatch latch = new CountDownLatch(1);
        mIsRunning = true;
        mSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) Log.d(TAG, "socket-thread [start]");

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
                        if (DEBUG) Log.d(TAG, "socket-thread [accept] ");
                        final Socket socket = mServerSocket.accept();
                        if (DEBUG) Log.d(TAG, "socket-thread [accept] " + socket);
                        mThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (DEBUG) Log.d(TAG, "socket-thread [pool-worker start] ");
                                    processConnection(socket);
                                    if (DEBUG) Log.d(TAG, "socket-thread [pool-worker end] ");
                                } catch (IOException e) {
                                    if (DEBUG) Log.d(TAG, "socket-thread [pool-worker] " + e);
                                }
                            }
                        });
                    }

                } catch (SocketException e) {
                    // It's expected to get SocketException due to socket.close here from stop()
                    if (DEBUG) Log.d(TAG, "socket-thread [expected] " + e);

                } catch (Throwable t) {
                    if (DEBUG) Log.d(TAG, "socket-thread [unexpected] " + t);

                } finally {
                    if (DEBUG) Log.d(TAG, "socket-thread [tear down]");
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
                        if (DEBUG) Log.d(TAG, "socket-thread [awaitTermination] " + e);
                    }
                }

                if (DEBUG) Log.d(TAG, "socket-thread [end]");
            }
        }, TAG + "-Thread");

        // start socket thread.
        // This will unlock the latch once the server connection is bound,
        // at which point we can get the listening socket address & port.
        mSocketThread.start();
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                // latch expired
                if (DEBUG) Log.d(TAG, "socket-thread [latch expired]");
                return null;
            }
        } catch (InterruptedException e) {
            // Interrupted while waiting...
            if (DEBUG) Log.d(TAG, "socket-thread [latch interrupted] " + e);
            return null;
        }
        InetSocketAddress address = (InetSocketAddress) mServerSocket.getLocalSocketAddress();
        if (DEBUG) Log.d(TAG, "socket-thread listening on " + address);
        return address;
    }

    /** Ask server to stop. Returns once the server is stopped. */
    public void stopSync() {
        if (mIsRunning) {
            stopAsync();
            try {
                if (DEBUG) Log.d(TAG, "stop -| join, state=" + mSocketThread.getState());
                mSocketThread.join();
                if (DEBUG) Log.d(TAG, "stop -| joined");
                mSocketThread = null;
            } catch (Throwable t) {
                Log.d(TAG, "stop -| join interrupted: " + t);
            }
        }
    }

    /** Ask server to stop. Returns once the server is stopped. */
    public void stopAsync() {
        if (DEBUG) Log.d(TAG, "stop -| isRunning=" + mIsRunning);
        if (mIsRunning) {
            mIsRunning = false;
            if (DEBUG) Log.d(TAG, "stop -| socket.close, thread.state=" + mSocketThread.getState());
            ServerSocket socket = mServerSocket;
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    if (DEBUG) Log.d(TAG, "stop -| socket.close=" + e);
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
            if (DEBUG) Log.d(TAG, "Added sender " + senderIndex);

            if (mOnClientConnectedRunnable != null) {
                try {
                    mOnClientConnectedRunnable.run();
                } catch (Exception ignore) {}
            }

            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "Writer thread started");
                    while (socket.isConnected() && !socket.isClosed() && mIsRunning) {
                        try {
                            String line = outCommands.takeFirst();
                            if (line != null) {
                                if (DEBUG_VERBOSE) Log.d(TAG, "WRITE << " + line.trim());
                                out_.println(line);
                                out_.flush();
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (DEBUG) Log.d(TAG, "Writer thread ended");
                }
            });

            sender.sendInit();

            while (socket.isConnected() && mIsRunning && !Thread.interrupted()) {
                String line = in.readLine();
                try {
                    mProtocol.processLine(sender, line);
                } catch (KeyValueProtocol.QCloseRequestException e) {
                    if (DEBUG) Log.d(TAG, "Q Close Request received.");
                    break;
                } catch (Exception e) {
                    if (DEBUG) Log.d(TAG, "Malformed line '" + line + "': " + e);
                }
            }
        } finally {
            if (senderIndex != -1) {
                synchronized (mSenders) {
                    mSenders.remove(senderIndex);
                }
                if (DEBUG) Log.d(TAG, "Removed sender " + senderIndex);

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

    private void broadcastChangeToAllSenders(@NonNull String key, @Null String value) {
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
