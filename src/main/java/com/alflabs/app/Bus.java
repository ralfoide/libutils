/*
 * (c) ralfoide gmail com, 2008
 * Project: TimerApp
 * License TBD
 */

package com.alflabs.app;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.alflabs.annotations.Null;
import com.alflabs.utils.Utils;

//-----------------------------------------------

/**
 * A simplistic message-based notifier.
 * <p/>
 * In its most basic implementation, the "bus" sends messages.
 * Listeners listen to the messages and are notified when a message is sent.
 * <p/>
 * Messages are objects (typically strings or integers).
 *
 * Messages are not queued like a Handler: posted messages are sent right away and deliver
 * to whoever is currently listening. There is no fancy thread handling like in a Handler
 * either: messages are posted and received on the sender's thread. If you want the facilities
 * of a Handler then use a Handler.
 * <p/>
 * Listeners can choose to listen to any kind of message, specific message
 * objects or just a given Class of message (e.g. Strings, or whatever custom type.)
 */
public class Bus {

    private static final String TAG = Bus.class.getSimpleName();
    private static final boolean DEBUG = Utils.isEmulator();

    public static final int NO_WHAT = -1;

    /**
     * Listeners. A map filter-type => listener list.
     * Access to the map must synchronized on the map itself.
     * Access to the lists must synchronized on the lists themselves.
     */
    private final HashMap<Class<?>, List<BusAdapter>> mListeners =
        new HashMap<Class<?>, List<BusAdapter>>();

    public Bus() {
    }

    // -----

    public void send(int what) {
        this.send(what, null);
    }

    public void send(Object object) {
        this.send(NO_WHAT, null);
    }

    public void send(int what, Object object) {
        Class<?> filter = object == null ? Void.class : object.getClass();

        if (DEBUG) {
            Log.d(TAG, String.format("Bus.send: what: %d, obj: %s", what, object));
        }

        List<BusAdapter> list0 = null;  // list of listeners with null.
        List<BusAdapter> list1 = null;  // list of typed listeners, including Void.class
        synchronized (mListeners) {
            list0 = mListeners.get(null);
            list1 = mListeners.get(filter);
        }
        sendInternal(list0, what, object);
        if (list1 != list0) {
            sendInternal(list1, what, object);
        }
    }

    private void sendInternal(List<BusAdapter> list, int what, Object object) {
        if (list != null) {
            synchronized (list) {
                for (BusAdapter listener : list) {
                    try {
                        listener.onBusMessage(what, object);
                    } catch(Throwable tr) {
                        Log.d(TAG, listener.getClass().getSimpleName() + ".onBusMessage failed", tr);
                    }
                }
            }
        }
    }

    // -----

    public BusAdapter addListener(BusAdapter listener) {
        if (listener == null) return listener;

        Class<?> filter = listener.getClassFilter();

        List<BusAdapter> list = null;
        synchronized (mListeners) {
            list = mListeners.get(filter);
            if (list == null) {
                list = new LinkedList<BusAdapter>();
                mListeners.put(filter, list);
            }
        }
        synchronized (list) {
            if (!list.contains(listener)) {
                list.add(listener);
            }
        }

        return listener;
    }

    public BusAdapter removeListener(BusAdapter listener) {
        if (listener == null) return listener;

        Class<?> filter = listener.getClassFilter();

        List<BusAdapter> list = null;
        synchronized (mListeners) {
            list = mListeners.get(filter);
        }
        if (list != null) {
            synchronized (list) {
                list.remove(listener);
            }
        }

        return listener;
    }

    // -----

    /**
     * Registers a BusListener with no class filter.
     *
     * @param receiver A receiver object implement {@link IBusListener}.
     * @param bus The {@link Bus} instance on which to register.
     * @return A new {@link Register} object; caller must call {@link Register#deregister()} later.
     */
    public static <B extends IBusListener> Register<B> register(B receiver, Bus bus) {
        return register(null, receiver, bus);
    }

    /**
     * Registers a BusListener with a specific class filter.
     *
     * @param classFilter The object class to filter. Can be null to receive everything (any kind
     *      of object, including null) or the {@link Void} class to filter on message with null
     *      objects.
     * @param receiver A receiver object implement {@link IBusListener}.
     * @param bus The {@link Bus} instance on which to register.
     * @return A new {@link Register} object; caller must call {@link Register#deregister()} later.
     */
    public static <B extends IBusListener> Register<B> register(@Null Class<?> classFilter, B receiver, Bus bus) {
        Register<B> m = new Register<B>(classFilter, receiver, bus);
        bus.addListener(m);
        return m;
    }

    /**
     * Helper to add/remove a bus listener on an existing class.
     * Usage:
     * <pre>
     * public class MyActivity extends Activity implements BusAdapter {
     *   private Bus.Register<MyActivity> mBusReg;
     *   public void onCreate(...) {
     *     mBusReg = Bus.Register.register(this, globals.getBus());
     *   }
     *   public void onDestroy() {
     *     mBusReg.deregister();
     *   }
     *   public void onBusMessage(int what, Object object) { ... }
     *   public void foo() {
     *      mBusReg.getBus().send(42);
     *   }
     * }
     * </pre>
     *
     * This class only keeps weak references on the listener (typically an activity) and the bus.
     */
    public static class Register<T extends IBusListener> extends BusAdapter {
        private WeakReference<Bus> mBusRef;
        private WeakReference<T>   mReceiverRef;

        private Register(@Null Class<?> classFilter, T receiver, Bus bus) {
            mReceiverRef = new WeakReference<T>(receiver);
            mBusRef = new WeakReference<Bus>(bus);
        }

        @Null
        public Bus getBus() {
            return mBusRef.get();
        }

        public void deregister() {
            if (mBusRef == null) return;

            Bus bus = mBusRef.get();
            if (bus != null) bus.removeListener(this);

            mBusRef.clear();
            mBusRef = null;

            if (mReceiverRef != null) {
                synchronized (this) {
                    mReceiverRef.clear();
                    mReceiverRef = null;
                }
            }
        }

        @Override
        public void onBusMessage(int what, Object object) {
            T receiver = null;
            synchronized (this) {
                if (mReceiverRef != null) receiver = mReceiverRef.get();
            }
            if (receiver != null) receiver.onBusMessage(what, object);
        }
    }

}


