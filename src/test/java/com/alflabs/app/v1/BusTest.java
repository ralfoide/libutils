package com.alflabs.app.v1;

import com.alflabs.annotations.Null;
import com.alflabs.app.v1.Bus;
import com.alflabs.app.v1.BusAdapter;
import com.alflabs.app.v1.Buses;
import com.alflabs.app.v1.IBusListener;
import static com.google.common.truth.Truth.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BusTest {

    private Bus b;
    private final List<String> allReceived = new ArrayList<String>();
    private final List<String> stringsReceived = new ArrayList<String>();
    private IBusListener all;
    private IBusListener strings;

    @Before
    public void setUp() throws Exception {
        b = Buses.newBus();

        all = new IBusListener() {
            @Override
            public void onBusMessage(int what, @Null Object object) {
                allReceived.add(String.format("%d: %s", what, object));
            }
        };

        strings = new IBusListener() {
            @Override
            public void onBusMessage(int what, @Null Object object) {
                stringsReceived.add(String.format("%d: %s", what, object));
            }
        };
    }

    @SuppressWarnings("UnnecessaryBoxing")
    @Test
    public void testRegister() throws Exception {
        assertThat(Arrays.toString(allReceived.toArray())).isEqualTo("[]");
        assertThat(Arrays.toString(stringsReceived.toArray())).isEqualTo("[]");

        b.register(all);
        b.register(String.class, strings);

        Bus.Sender sender = b.getSender();
        assertThat(sender).isNotNull();
        assertThat(sender.getBus()).isNotNull();
        assertThat(sender.getBus()).isSameAs(b);

        sender.safeSend(42);
        sender.safeSend(43, "some strings");
        sender.safeSend("moar strings");
        sender.safeSend(44, Integer.valueOf(44));

        assertThat(Arrays.toString(allReceived.toArray())).isEqualTo(
                "[42: null, 43: some strings, -1: moar strings, 44: 44]");
        assertThat(Arrays.toString(stringsReceived.toArray())).isEqualTo(
                "[43: some strings, -1: moar strings]");

        b = null;
        System.gc();
        System.gc();
        System.gc();

        sender.safeSend(45, "weak ref");
        sender.safeSend(46, Integer.valueOf(46));

        assertThat(Arrays.toString(allReceived.toArray())).isEqualTo(
                "[42: null, 43: some strings, -1: moar strings, 44: 44]");
        assertThat(Arrays.toString(stringsReceived.toArray())).isEqualTo(
                "[43: some strings, -1: moar strings]");
    }

    @SuppressWarnings("UnnecessaryBoxing")
    @Test
    public void testUnregister() throws Exception {
        assertThat(Arrays.toString(allReceived.toArray())).isEqualTo("[]");
        assertThat(Arrays.toString(stringsReceived.toArray())).isEqualTo("[]");

        b.register(all);
        b.register(String.class, strings);

        Bus.Sender sender = b.getSender();
        assertThat(sender).isNotNull();
        assertThat(sender.getBus()).isNotNull();
        assertThat(sender.getBus()).isSameAs(b);

        sender.safeSend(42);
        sender.safeSend(43, "some strings");
        sender.safeSend("moar strings");
        sender.safeSend(44, Integer.valueOf(44));

        assertThat(Arrays.toString(allReceived.toArray())).isEqualTo(
                "[42: null, 43: some strings, -1: moar strings, 44: 44]");
        assertThat(Arrays.toString(stringsReceived.toArray())).isEqualTo(
                "[43: some strings, -1: moar strings]");

        b.unregister(all);
        sender.safeSend(45, "unregister all");

        assertThat(Arrays.toString(allReceived.toArray())).isEqualTo(
                "[42: null, 43: some strings, -1: moar strings, 44: 44]");
        assertThat(Arrays.toString(stringsReceived.toArray())).isEqualTo(
                "[43: some strings, -1: moar strings, 45: unregister all]");

        b.unregister(strings);
        sender.safeSend(45, "unregister string");

        assertThat(Arrays.toString(allReceived.toArray())).isEqualTo(
                "[42: null, 43: some strings, -1: moar strings, 44: 44]");
        assertThat(Arrays.toString(stringsReceived.toArray())).isEqualTo(
                "[43: some strings, -1: moar strings, 45: unregister all]");
    }

    @SuppressWarnings("UnnecessaryBoxing")
    @Test
    public void testReentrant() throws Exception {
        assertThat(Arrays.toString(allReceived.toArray())).isEqualTo("[]");
        assertThat(Arrays.toString(stringsReceived.toArray())).isEqualTo("[]");

        b.register(new BusAdapter() {
            @Override
            public void onBusMessage(int what, @Null Object object) {
                // modify the class-agnostic list from withing the message sender.
                b.register(new BusAdapter() {
                    @Override
                    public void onBusMessage(int what, @Null Object object) {}
                });
                // modify a class-specific list from withing the message sender.
                b.register(String.class, strings);
            }
        });
        b.register(all);

        Bus.Sender sender = b.getSender();
        assertThat(sender).isNotNull();
        assertThat(sender.getBus()).isNotNull();
        assertThat(sender.getBus()).isSameAs(b);

        // This calls the first listener which itself registers the string listener.
        // That means there's a race condition but the sender is designed to allow for
        // this and avoid ConcurrentModificationException.
        sender.safeSend(42);

        // the all listener should have been invoked but the string one not yet
        // as all the messages are sent after the listeners are collected.
        assertThat(Arrays.toString(allReceived.toArray())).isEqualTo("[42: null]");
        assertThat(Arrays.toString(stringsReceived.toArray())).isEqualTo("[]");

        // now they should both be invoked.
        sender.safeSend(43, "foo");
        assertThat(Arrays.toString(allReceived.toArray())).isEqualTo("[42: null, 43: foo]");
        assertThat(Arrays.toString(stringsReceived.toArray())).isEqualTo("[43: foo]");
    }
}
