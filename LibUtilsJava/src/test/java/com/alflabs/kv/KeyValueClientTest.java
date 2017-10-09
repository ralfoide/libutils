package com.alflabs.kv;

import com.alflabs.annotations.LargeTest;
import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.rx.Schedulers;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.JavaClock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

@LargeTest
public class KeyValueClientTest {
    private static final String TAG = KeyValueClientTest.class.getSimpleName();

    private KeyValueServer mServer;
    private KeyValueClient mClient;
    private final List<String> mServerChanges = new ArrayList<>();
    private final List<String> mClientChanges = new ArrayList<>();

    public void logD(String tag, String msg) {
        System.out.println(tag + ": " + msg + "\n");
    }

    @Before
    public void setUp() throws Exception {
        mClientChanges.clear();
        mServerChanges.clear();
        mClient = null;
        mServer = new KeyValueServer(mock(ILogger.class));
        mServer.getChangedStream().subscribe((stream, key) -> {
            assert key != null;
            mServerChanges.add(key + "=" + mServer.getValue(key));
        }, Schedulers.sync());
    }

    @After
    public void tearDown() throws Exception {
        if (mClient != null) {
            logD(TAG, "tearDown client stop");
            mClient.stopSync();
            mClient = null;
        }
        logD(TAG, "tearDown server stop");
        mServer.stopSync();
        mServer = null;
    }

    @NonNull
    private String _serverChanges() throws InterruptedException {
        Thread.sleep(100 /*ms*/);
        try {
            return Arrays.toString(mServerChanges.toArray());
        } finally {
            mServerChanges.clear();
        }
    }

    @NonNull
    private String _clientChanges() throws InterruptedException {
        Thread.sleep(100 /*ms*/);
        try {
            return Arrays.toString(mClientChanges.toArray());
        } finally {
            mClientChanges.clear();
        }
    }

    @Test
    public void testKeyValueClientTest_Protocol() throws Exception {
        InetSocketAddress address = mServer.start(20005);
        assertThat(address).isNotNull();

        mClient = new KeyValueClient(
                new JavaClock(),
                mock(ILogger.class),
                address,
                new KeyValueClient.IStatsListener() {
            @Override
            public void addBandwidthTXBytes(int count) {}

            @Override
            public void addBandwidthRXBytes(int count) {}

            @Override
            public void setMessage(@Null String msg) {}

            @Override
            public void HBLatencyRequestSent() {}

            @Override
            public void HBLatencyReplyReceived() {}
        });
        mClient.getChangedStream().subscribe((stream, key) -> {
            assert key != null;
            mClientChanges.add(key + "=" + mClient.getValue(key));
        }, Schedulers.sync());
        mClient.startAsync();

        Thread.sleep(100 /*ms*/);
        assertThat(mServer.getNumConnections()).isEqualTo(1);

        mClient.requestAllKeys();
        assertThat(_clientChanges()).isEqualTo("[]");
        assertThat(mClient.getValue("foo")).isNull();

        mServer.putValue("foo", "bar", false /*broadcast*/);
        mServer.putValue("key 1", "value 1", false /*broadcast*/);
        mServer.putValue("key 2", "value 2", false /*broadcast*/);
        mClient.requestAllKeys();
        assertThat(_clientChanges()).isEqualTo("[foo=bar, key 1=value 1, key 2=value 2]");
        assertThat(mClient.getValue("foo")).isEqualTo("bar");

        mServer.putValue("foo", "blah", true /*broadcast*/);
        assertThat(_clientChanges()).isEqualTo("[foo=blah]");
        assertThat(mClient.getValue("foo")).isEqualTo("blah");

        mClient.requestKey("foo");
        assertThat(_clientChanges()).isEqualTo("[]"); // value has not changed, so callback not invoked

        mClient.putValue("foo", "bar2", true /*broadcast*/);
        Thread.sleep(100 /*ms*/);
        assertThat(_serverChanges()).isEqualTo("[foo=bar2]");
        assertThat(mServer.getValue("foo")).isEqualTo("bar2");

        // Q closes the connection but not the server
        logD(TAG, "KeyValueServerTest_Protocol: start Q request");
        mClient.stopSync();

        logD(TAG, "KeyValueServerTest_Protocol: pause for socket disconnect");
        Thread.sleep(100 /*ms*/);
        assertThat(mServer.isRunning()).isTrue();
        assertThat(mServer.getNumConnections()).isEqualTo(0);

        assertThat(new TreeSet<>(mClient.getKeys()).toArray()).isEqualTo(new String[] {
                "foo", "key 1", "key 2"
        });
        assertThat(mClient.getValue("bar")).isNull();
        assertThat(mClient.getValue("foo")).isEqualTo("bar2");
        assertThat(mClient.getValue("key 1")).isEqualTo("value 1");
        assertThat(mClient.getValue("key 2")).isEqualTo("value 2");
    }
}
