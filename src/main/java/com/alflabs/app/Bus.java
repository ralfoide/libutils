/*
 * (c) ralfoide gmail com, 2008
 * Project: TimerApp
 * License TBD
 */

package com.alflabs.app;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
 * <p/>
 * Messages are not queued like a Handler: posted messages are sent right away and delivered
 * to whoever is currently listening. There is no fancy thread handling like in a Handler
 * either: messages are posted and received on the sender's thread.
 * If you want the facilities of a Handler then use a Handler.
 * <p/>
 * Listeners can choose to listen to any kind of message, specific message
 * objects or just a given Class of message (e.g. Strings, or whatever custom type.)
 * <p/>
 * All created buses are collected in a weak reference list, with each a different id.
 * At runtime it is thus possible to get an existing bus from its id, if the object is
 * still alive.
 */
public class Bus {

    private static final String TAG = Bus.class.getSimpleName();
    private static final boolean DEBUG = Utils.isEmulator();

    public static final int NO_WHAT = -1;

    private static final List<WeakReference<Bus>> sBuses = new ArrayList<WeakReference<Bus>>();

    private final int mBusId;

    /**
     * Listeners. A map filter-type => listener list.
     * Access to the map must synchronized on the map itself.
     * Access to the lists must synchronized on the lists themselves.
     */
    private final HashMap<Class<?>, List<BusAdapter>> mListeners = new HashMap<Class<?>, List<BusAdapter>>();

    public Bus() {
        synchronized (sBuses) {
            mBusId = sBuses.size();
            sBuses.add(new WeakReference<Bus>(this));
        }
    }

    public int getId() { return mBusId; }

    public static Bus getById(int id) {
        synchronized (sBuses) {
            for (WeakReference<Bus> wb : sBuses) {
                Bus b = wb.get();
                if (b != null && b.getId() == id) {
                    return b;
                }
            }
        }

        return null;
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

    @Null
    public BusAdapter addListener(BusAdapter listener) {
        if (listener == null) return null;

        Class<?> filter = listener.getClassFilter();

        List<BusAdapter> list = null;
        synchronized (mListeners) {
            list = mListeners.get(filter);
            if (list == null) {
                list = new LinkedList<BusAdapter>();
                mListeners.put(filter, list);
            }
        }
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (list) {
            if (!list.contains(listener)) {
                list.add(listener);
            }
        }

        return listener;
    }

    @Null
    public BusAdapter removeListener(BusAdapter listener) {
        if (listener == null) return null;

        Class<?> filter = listener.getClassFilter();

        List<BusAdapter> list = null;
        synchronized (mListeners) {
            list = mListeners.get(filter);
        }
        if (list != null) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
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
     * @return A new {@link Reference} object; caller must call {@link Reference#unregister()} later.
     */
    public Reference register(IBusListener receiver) {
        return register(null, receiver);
    }

    /**
     * Registers a BusListener with a specific class filter.
     *
     * @param classFilter The object class to filter. Can be null to receive everything (any kind
     *      of object, including null) or the {@link Void} class to filter on message with null
     *      objects.
     * @param receiver A receiver object implement {@link IBusListener}.
     * @return A new {@link Reference} object; caller must call {@link Reference#unregister()} later.
     */
    public Reference register(@Null Class<?> classFilter, IBusListener receiver) {
        Reference m = new Reference(classFilter, receiver, this);
        this.addListener(m);
        return m;
    }

    /**
     * Helper to use a weak bus reference.
     * Usage:
     * <pre>
     * public class MyActivity extends Activity implements IBusListener {
     *   private Bus.Reference mBusRef;
     *   public void onCreate(...) {
     *     mBusRef = globals.getBus().register(this);
     *   }
     *   public void onDestroy() {
     *     mBusRef.unregister();  // will NOT crash if bus no longer exists
     *   }
     *   public void onBusMessage(int what, Object object) { ... }
     *   public void foo() {
     *      mBusRef.getBus().send(42);  // will npe if bus no longer exists
     *      mBusRef.safeSend(42);       // will NOT npe if bus no longer exists
     *   }
     * }
     * </pre>
     *
     * This class only keeps weak references on the listener (typically an activity) and the bus.
     * That means the bus won't crash if the listener is gone and the caller won't crash
     * if trying to send or received on a released bus either.
     */
    public static class Reference extends BusAdapter {
        private WeakReference<Bus>          mBusRef;
        private WeakReference<IBusListener> mReceiverRef;

        private Reference(@Null Class<?> classFilter, IBusListener receiver, Bus bus) {
            mReceiverRef = new WeakReference<IBusListener>(receiver);
            mBusRef = new WeakReference<Bus>(bus);
        }

        /** Returns the bus referenced. Might be null if the weak reference is gone. */
        @Null
        public Bus getBus() {
            return mBusRef.get();
        }

        /**
         * Safely unregisters this reference.
         * Will not thrown an exception if the underlying Bus has released. */
        public void unregister() {
            if (mBusRef == null) {
                return;
            }

            Bus bus = mBusRef.get();
            if (bus != null) {
                bus.removeListener(this);
            }

            mBusRef.clear();
            mBusRef = null;

            if (mReceiverRef != null) {
                synchronized (this) {
                    mReceiverRef.clear();
                    mReceiverRef = null;
                }
            }
        }

        public void safeSend(int what) {
            this.safeSend(what, null);
        }

        public void safeSend(Object object) {
            this.safeSend(NO_WHAT, null);
        }

        public void safeSend(int what, Object object) {
            if (mBusRef == null) {
                return;
            }

            Bus bus = mBusRef.get();
            if (bus != null) {
                bus.send(what, object);
            }
        }

        /** Internal helper. Calls the receiver onBusMessage if it hasn't been released yet. */
        @Override
        public void onBusMessage(int what, Object object) {
            IBusListener receiver = null;
            synchronized (this) {
                if (mReceiverRef != null) receiver = mReceiverRef.get();
            }
            if (receiver != null) receiver.onBusMessage(what, object);
        }
    }

}


