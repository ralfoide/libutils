package com.alflabs.app;

import com.alflabs.annotations.Null;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.junit.Assert.*;

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
        assertEquals("[]", Arrays.toString(allReceived.toArray()));
        assertEquals("[]", Arrays.toString(stringsReceived.toArray()));

        b.register(all);
        b.register(String.class, strings);

        Bus.Sender sender = b.getSender();
        assertNotNull(sender);
        assertNotNull(sender.getBus());
        assertSame(b, sender.getBus());

        sender.safeSend(42);
        sender.safeSend(43, "some strings");
        sender.safeSend("moar strings");
        sender.safeSend(44, Integer.valueOf(44));

        assertEquals(
                "[42: null, 43: some strings, -1: moar strings, 44: 44]",
                Arrays.toString(allReceived.toArray()));
        assertEquals(
                "[43: some strings, -1: moar strings]",
                Arrays.toString(stringsReceived.toArray()));

        b = null;
        System.gc();
        System.gc();
        System.gc();

        sender.safeSend(45, "weak ref");
        sender.safeSend(46, Integer.valueOf(46));

        assertEquals(
                "[42: null, 43: some strings, -1: moar strings, 44: 44]",
                Arrays.toString(allReceived.toArray()));
        assertEquals(
                "[43: some strings, -1: moar strings]",
                Arrays.toString(stringsReceived.toArray()));
    }

    @SuppressWarnings("UnnecessaryBoxing")
    @Test
    public void testUnregister() throws Exception {
        assertEquals("[]", Arrays.toString(allReceived.toArray()));
        assertEquals("[]", Arrays.toString(stringsReceived.toArray()));

        b.register(all);
        b.register(String.class, strings);

        Bus.Sender sender = b.getSender();
        assertNotNull(sender);
        assertNotNull(sender.getBus());
        assertSame(b, sender.getBus());

        sender.safeSend(42);
        sender.safeSend(43, "some strings");
        sender.safeSend("moar strings");
        sender.safeSend(44, Integer.valueOf(44));

        assertEquals(
                "[42: null, 43: some strings, -1: moar strings, 44: 44]",
                Arrays.toString(allReceived.toArray()));
        assertEquals(
                "[43: some strings, -1: moar strings]",
                Arrays.toString(stringsReceived.toArray()));

        b.unregister(all);
        sender.safeSend(45, "unregister all");

        assertEquals(
                "[42: null, 43: some strings, -1: moar strings, 44: 44]",
                Arrays.toString(allReceived.toArray()));
        assertEquals(
                "[43: some strings, -1: moar strings, 45: unregister all]",
                Arrays.toString(stringsReceived.toArray()));

        b.unregister(strings);
        sender.safeSend(45, "unregister string");

        assertEquals(
                "[42: null, 43: some strings, -1: moar strings, 44: 44]",
                Arrays.toString(allReceived.toArray()));
        assertEquals(
                "[43: some strings, -1: moar strings, 45: unregister all]",
                Arrays.toString(stringsReceived.toArray()));
    }

    @SuppressWarnings("UnnecessaryBoxing")
    @Test
    public void testReentrant() throws Exception {
        assertEquals("[]", Arrays.toString(allReceived.toArray()));
        assertEquals("[]", Arrays.toString(stringsReceived.toArray()));

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
        assertNotNull(sender);
        assertNotNull(sender.getBus());
        assertSame(b, sender.getBus());

        // This calls the first listener which itself registers the string listener.
        // That means there's a race condition but the sender is designed to allow for
        // this and avoid ConcurrentModificationException.
        sender.safeSend(42);

        // the all listener should have been invoked but the string one not yet
        // as all the messages are sent after the listeners are collected.
        assertEquals("[42: null]", Arrays.toString(allReceived.toArray()));
        assertEquals("[]", Arrays.toString(stringsReceived.toArray()));

        // now they should both be invoked.
        sender.safeSend(43, "foo");
        assertEquals("[42: null, 43: foo]", Arrays.toString(allReceived.toArray()));
        assertEquals("[43: foo]", Arrays.toString(stringsReceived.toArray()));
    }
}
