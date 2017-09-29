package com.alflabs.kv;

import com.alflabs.annotations.NonNull;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.RPair;
import com.google.common.truth.Truth;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

public class KeyValueProtocolTest {
    private static final String TAG = KeyValueProtocolTest.class.getSimpleName();

    private KeyValueProtocol mProtocol;
    private RPair<String, String> mLastChanged;
    private KeyValueProtocol.Sender mSender;
    private final Map<String, Integer> mCounts = new TreeMap<>();
    private final List<String> mSent = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        mLastChanged = null;
        mSent.clear();
        mCounts.clear();
        mCounts.put("Ping", 0);
        mCounts.put("Quit", 0);
        mCounts.put("Read", 0);
        mCounts.put("Write", 0);

        mProtocol = new KeyValueProtocol(mock(ILogger.class)) {
            @Override
            protected void processPing(@NonNull Sender sender, @NonNull String line) {
                super.processPing(sender, line);
                mCounts.put("Ping", 1 + mCounts.get("Ping"));
            }

            @Override
            protected void processQuit() throws QCloseRequestException {
                try {
                    super.processQuit();
                } finally {
                    mCounts.put("Quit", 1 + mCounts.get("Quit"));
                }
            }

            @Override
            protected void processRead(@NonNull Sender sender, @NonNull String line) {
                super.processRead(sender, line);
                mCounts.put("Read", 1 + mCounts.get("Read"));
            }

            @Override
            protected void processWrite(@NonNull String line) {
                super.processWrite(line);
                mCounts.put("Write", 1 + mCounts.get("Write"));
            }
        };

        mProtocol.setOnChangeListener((key, value) -> {
            mLastChanged = RPair.create(key, value);
            if (value == null) value = "";
            mSender.sendValue(key, value);
        });

        mSender = new KeyValueProtocol.Sender() {
            @Override
            public void sendLine(@NonNull String line) {
                mSent.add(line);
            }
        };
    }

    @Test
    public void testKeyValueProtocolTest() throws Exception {
        assertThat(mCounts.toString()).isEqualTo("{Ping=0, Quit=0, Read=0, Write=0}");

        mProtocol.processLine(mSender, "R*");
        assertThat(_readAll()).isEqualTo("[]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=0, Quit=0, Read=1, Write=0}");

        assertThat(mProtocol.getValue("foo")).isNull();
        Truth.assertThat(mLastChanged).isNull();

        mProtocol.processLine(mSender, "  Wfoo:bar  ");
        assertThat(_readAll()).isEqualTo("[Wfoo:bar]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=0, Quit=0, Read=1, Write=1}");

        assertThat(mProtocol.getValue("foo")).isEqualTo("bar");
        Truth.assertThat(mLastChanged).isEqualTo(RPair.create("foo", "bar"));
        mLastChanged = null;

        // Writing the same value does not trigger a change notification
        mProtocol.processLine(mSender, "  Wfoo:bar  ");
        assertThat(_readAll()).isEqualTo("[]");
        Truth.assertThat(mLastChanged).isNull();
        assertThat(mCounts.toString()).isEqualTo("{Ping=0, Quit=0, Read=1, Write=2}");

        mProtocol.processLine(mSender, "R*");
        assertThat(_readAll()).isEqualTo("[Wfoo:bar]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=0, Quit=0, Read=2, Write=2}");

        mProtocol.putValue("key 1", "value 1");
        mProtocol.putValue("key 2", "value 2");
        assertThat(mProtocol.getValue("key 1")).isEqualTo("value 1");
        assertThat(mProtocol.getValue("key 2")).isEqualTo("value 2");

        mProtocol.processLine(mSender, "    R  *  ");
        assertThat(_readAll()).isEqualTo("[Wfoo:bar, Wkey 1:value 1, Wkey 2:value 2]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=0, Quit=0, Read=3, Write=2}");

        mProtocol.processLine(mSender, "  PS anything after PS is repeated as is even : or any $p3ci4|_ characters  ");
        assertThat(_readAll()).isEqualTo("[PR anything after PS is repeated as is even : or any $p3ci4|_ characters]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=1, Quit=0, Read=3, Write=2}");

        mProtocol.processLine(mSender, "  P ping without P+S prefix is simply ignored");
        assertThat(_readAll()).isEqualTo("[]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=2, Quit=0, Read=3, Write=2}");

        // This is not an ill-formatted line
        mProtocol.processLine(mSender, " W foo : bar : foo : bar  ");
        assertThat(_readAll()).isEqualTo("[Wfoo:bar : foo : bar]");
        assertThat(mProtocol.getValue("foo")).isEqualTo("bar : foo : bar");
        Truth.assertThat(mLastChanged).isEqualTo(RPair.create("foo", "bar : foo : bar"));
        assertThat(mCounts.toString()).isEqualTo("{Ping=2, Quit=0, Read=3, Write=3}");

        // send some ill-formatted lines
        mProtocol.processLine(mSender, "R");
        assertThat(_readAll()).isEqualTo("[]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=2, Quit=0, Read=4, Write=3}");

        mProtocol.processLine(mSender, "    R    ");
        assertThat(_readAll()).isEqualTo("[]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=2, Quit=0, Read=5, Write=3}");

        mProtocol.processLine(mSender, " W : bar  ");
        assertThat(_readAll()).isEqualTo("[]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=2, Quit=0, Read=5, Write=4}");

        mProtocol.processLine(mSender, " Wfoo  ");
        assertThat(_readAll()).isEqualTo("[]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=2, Quit=0, Read=5, Write=5}");

        boolean got_exception = false;
        try {
            mProtocol.processLine(mSender, "Q");
        } catch (KeyValueProtocol.QCloseRequestException expected) {
            got_exception = true;
        }
        assertThat(got_exception).isTrue();
        assertThat(_readAll()).isEqualTo("[]");
        assertThat(mCounts.toString()).isEqualTo("{Ping=2, Quit=1, Read=5, Write=5}");

        assertThat(new TreeSet<>(mProtocol.getKeys()).toArray()).isEqualTo(new String[] {
                "foo", "key 1", "key 2"
        });
        assertThat(mProtocol.getValue("bar")).isNull();
        assertThat(mProtocol.getValue("foo")).isEqualTo("bar : foo : bar");
        assertThat(mProtocol.getValue("key 1")).isEqualTo("value 1");
        assertThat(mProtocol.getValue("key 2")).isEqualTo("value 2");
    }

    /** Read all it can from out till it blocks. */
    private String _readAll() {
        try {
            return Arrays.toString(mSent.toArray());
        } finally {
            mSent.clear();
        }
    }
}
