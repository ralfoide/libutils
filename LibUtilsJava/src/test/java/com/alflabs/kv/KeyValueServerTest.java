package com.alflabs.kv;

import com.alflabs.annotations.LargeTest;
import com.alflabs.annotations.NonNull;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.RPair;
import com.google.common.truth.Truth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

@LargeTest
public class KeyValueServerTest {
    private static final String TAG = KeyValueServerTest.class.getSimpleName();

    private KeyValueServer mServer;
    private RPair<String, String> mLastChanged;
    private final AtomicInteger mOnConnectedCallCount = new AtomicInteger();

    public void logD(String tag, String msg) {
        System.out.println(tag + ": " + msg + "\n");
    }

    @Before
    public void setUp() throws Exception {
        mLastChanged = null;
        mOnConnectedCallCount.set(0);
        mServer = new KeyValueServer(mock(ILogger.class));
        mServer.setOnChangeListener((key, value) -> mLastChanged = RPair.create(key, value));
        mServer.setOnClientConnected(() -> mOnConnectedCallCount.incrementAndGet());
    }

    @After
    public void tearDown() throws Exception {
        logD(TAG, "tearDown server stop");
        mServer.stopSync();
        mServer = null;
    }

    @Test
    public void testKeyValueServerTest_StartStop1() throws Exception {
        assertThat(mServer.isRunning()).isFalse();
        assertThat(mServer.getNumConnections()).isEqualTo(0);

        InetSocketAddress address = mServer.start(20005);
        assertThat(address).isNotNull();
        assertThat(mServer.isRunning()).isTrue();
        assertThat(address.getPort()).isNotEqualTo(0);
        assertThat(address.getAddress().getHostAddress()).isNotEmpty();
        assertThat(mServer.getNumConnections()).isEqualTo(0);

        mServer.stopSync();
        assertThat(mServer.isRunning()).isFalse();
        assertThat(mServer.getNumConnections()).isEqualTo(0);
    }

    @Test
    public void testKeyValueServerTest_StartStop2() throws Exception {
        assertThat(mServer.isRunning()).isFalse();
        assertThat(mServer.getNumConnections()).isEqualTo(0);

        // stopping when not started does nothing
        mServer.stopSync();
        assertThat(mServer.isRunning()).isFalse();
        assertThat(mServer.getNumConnections()).isEqualTo(0);

        InetSocketAddress address = mServer.start(20005);
        assertThat(address).isNotNull();
        assertThat(mServer.isRunning()).isTrue();
        assertThat(address.getPort()).isNotEqualTo(0);
        assertThat(address.getAddress().getHostAddress()).isNotEmpty();
        assertThat(mServer.getNumConnections()).isEqualTo(0);

        // can't start when it's already started
        assertThat(mServer.start(20005)).isNull();

        mServer.stopSync();
        assertThat(mServer.isRunning()).isFalse();
        assertThat(mServer.getNumConnections()).isEqualTo(0);

        // stopping one more time does nothing
        mServer.stopSync();
        assertThat(mServer.isRunning()).isFalse();
    }

    @Test
    public void testKeyValueServerTest_Protocol() throws Exception {
        InetSocketAddress address = mServer.start(20005);
        assertThat(address).isNotNull();

        Socket socket = new Socket(address.getAddress(), address.getPort());
        assertThat(socket).isNotNull();

        try {
            Thread.sleep(100 /*ms*/);
            assertThat(socket.isConnected()).isTrue();
            assertThat(mServer.getNumConnections()).isEqualTo(1);
            assertThat(mOnConnectedCallCount.get()).isEqualTo(1);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            assertThat(_readAll(in)).isEqualTo("[VJuniorDayModelServer:1]");

            _sendLine(out, "R*");
            assertThat(_readAll(in)).isEqualTo("[]");

            assertThat(mServer.getValue("foo")).isNull();
            Truth.assertThat(mLastChanged).isNull();

            _sendLine(out, "  Wfoo:bar  ");
            assertThat(_readAll(in)).isEqualTo("[Wfoo:bar]");

            assertThat(mServer.getValue("foo")).isEqualTo("bar");
            Truth.assertThat(mLastChanged).isEqualTo(RPair.create("foo", "bar"));
            mLastChanged = null;

            // Writing the same value does not trigger a change notification
            _sendLine(out, "  Wfoo:bar  ");
            assertThat(_readAll(in)).isEqualTo("[]");
            Truth.assertThat(mLastChanged).isNull();

            _sendLine(out, "R*");
            assertThat(_readAll(in)).isEqualTo("[Wfoo:bar]");

            mServer.putValue("key 1", "value 1", false /*broadcast*/);
            mServer.putValue("key 2", "value 2", false /*broadcast*/);
            assertThat(mServer.getValue("key 1")).isEqualTo("value 1");
            assertThat(mServer.getValue("key 2")).isEqualTo("value 2");

            _sendLine(out, "    R  *  ");
            assertThat(_readAll(in)).isEqualTo("[Wfoo:bar, Wkey 1:value 1, Wkey 2:value 2]");

            _sendLine(out, "  PS anything after P is repeated as is even : or any $p3ci4|_ characters  ");
            assertThat(_readAll(in)).isEqualTo("[PR anything after P is repeated as is even : or any $p3ci4|_ characters]");

            // This is not an ill-formatted line
            _sendLine(out, " W foo : bar : foo : bar  ");
            assertThat(_readAll(in)).isEqualTo("[Wfoo:bar : foo : bar]");
            assertThat(mServer.getValue("foo")).isEqualTo("bar : foo : bar");
            Truth.assertThat(mLastChanged).isEqualTo(RPair.create("foo", "bar : foo : bar"));

            // send some ill-formatted lines
            _sendLine(out, "R");
            assertThat(_readAll(in)).isEqualTo("[]");

            _sendLine(out, "    R    ");
            assertThat(_readAll(in)).isEqualTo("[]");

            _sendLine(out, " W : bar  ");
            assertThat(_readAll(in)).isEqualTo("[]");

            _sendLine(out, " Wfoo  ");
            assertThat(_readAll(in)).isEqualTo("[]");


            // Q closes the connection but not the server
            logD(TAG, "KeyValueServerTest_Protocol: start Q request");
            assertThat(socket.isConnected()).isTrue();
            _sendLine(out, "Q");
            assertThat(_readAll(in)).isEqualTo("[]");

            logD(TAG, "KeyValueServerTest_Protocol: pause for socket disconnect");
            Thread.sleep(100 /*ms*/);
            assertThat(mServer.isRunning()).isTrue();
            assertThat(mServer.getNumConnections()).isEqualTo(0);
            assertThat(mOnConnectedCallCount.get()).isEqualTo(2);

        } finally {
            logD(TAG, "KeyValueServerTest_Protocol: finally socket stop");
            socket.close();
        }
        // Oddly enough socket.isConnected remains true on at least one tablet device
        // although the canonical implementation of socket.close clearly sets the flag
        // to false. works as expected on emulator. Moving on.
        // assertThat(socket.isConnected()).isFalse();
    }

    private void _sendLine(@NonNull PrintWriter out, @NonNull String line) {
        out.println(line);
        out.flush();
    }

    /** Read all it can from out till it blocks. */
    private String _readAll(@NonNull BufferedReader in) throws IOException, InterruptedException {
        Thread.sleep(100 /*ms*/);
        ArrayList<String> lines = new ArrayList<>();
        while (in.ready()) {
            lines.add(in.readLine());
        }
        return Arrays.toString(lines.toArray());
    }
}
