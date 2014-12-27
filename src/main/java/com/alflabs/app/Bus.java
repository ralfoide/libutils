/*
 * (c) ralfoide gmail com, 2008
 * Project: TimerApp
 * License TBD
 */

package com.alflabs.app;

import java.lang.ref.WeakReference;
import java.util.*;

import android.util.Log;

import com.alflabs.annotations.NonNull;
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
 * <p/>
 * Usage: <br/>
 * - Use Buses.newBus() to create a new bus instance. Hold the reference as long as needed. <br/>
 * - Use Bus.register(listener) to add a listener to the bus. <br/>
 *   The bus keeps a weak reference on the listener and will remove it if the listener
 *   is no longer reachable. <br/>
 * - Use Bus.unregister(listener) to remove the listener later. <br/>
 * - Use Bus.getReference() to get a weak reference on the bus that can be passed around
 *   without holding the bus. <br/>
 * - Use Bus.getSender() to get a sender object. This one also maintains a weak reference
 *   on the bus. <br/>
 * <p/>
 * Example of typical usage:
 * <pre>
 *  Globals:
 *      private static final Bus mGlobalBus = Buses.newBus();
 *      public static Bus getBus() { return mGlobalBus; }
 *  Activity:
 *      public class MyActivity extends Activity implements IBusListener {
 *         private final Bus.Reference mBusRef = Globals.getBus().getReference().register(this);
 *         private final Bus.Sender mBusSender = mBusRef.getSender();
 *         ...
 *         mBusSender.safeSend(...)
 *         ...
 *         void onDestroy() { mBusRef.unregister(this); }
 *
 *         // remember that onBusMessage is invoked on the thread of the sender
 *         // which may not be the UI thread so always use runOnUiThread or similar.
 *         void onBusMessage(final int what, @Null final Object object) {
 *           runOnUiThread(new Runnable() {
 *             public void run() {
 *               switch(what) {
 *               // process messages
 *               }
 *             }
 *           }
 *         }
 *      }
 * </pre>
 */
public class Bus {

    private static final String TAG = Bus.class.getSimpleName();
    private static final boolean DEBUG = Utils.isEmulator();

    public static final int NO_WHAT = -1;

    private final int mBusId;

    /**
     * Listeners. A map filter-type => listener list.
     * Access to the map must synchronized on the map itself.
     * Access to the lists must synchronized on the lists themselves.
     */
    private final HashMap<Class<?>, List<WeakReference<IBusListener>>> mListeners =
            new HashMap<Class<?>, List<WeakReference<IBusListener>>>();

    /** To create a Bus instance, use {@code Buses.newBus()}. */
    Bus(int id) {
        mBusId = id;
    }

    public int getId() { return mBusId; }

    // -----

    private void _send(int what, Object object) {
        Class<?> filter = object == null ? Void.class : object.getClass();

        if (DEBUG) Log.d(TAG, String.format("Bus.send: what: %d, obj: %s", what, object));

        List<WeakReference<IBusListener>> list0 = null;  // list of listeners with null.
        List<WeakReference<IBusListener>> list1 = null;  // list of typed listeners, including Void.class
        synchronized (mListeners) {
            list0 = mListeners.get(null);
            list1 = mListeners.get(filter);
        }
        _sendInternal(list0, what, object);
        if (list1 != list0) {
            _sendInternal(list1, what, object);
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private void _sendInternal(List<WeakReference<IBusListener>> list, int what, Object object) {
        if (list != null) {
            synchronized (list) {
                for (Iterator<WeakReference<IBusListener>> it = list.iterator(); it.hasNext(); ) {
                    WeakReference<IBusListener> wr =  it.next();
                    IBusListener listener = wr.get();
                    if (listener == null) {
                        it.remove();
                    } else {
                        try {
                            listener.onBusMessage(what, object);
                        } catch(Throwable tr) {
                            Log.d(TAG, listener.getClass().getSimpleName() + ".onBusMessage failed", tr);
                        }
                    }
                }
            }
        }
    }

    // -----

    /**
     * Registers a BusListener with no class filter.
     *
     * @param listener A receiver object implement {@link com.alflabs.app.IBusListener}
     *                 or {@link com.alflabs.app.BusAdapter}.
     * @return self for chaining.
     */
    @NonNull
    public Bus register(IBusListener listener) {
        _addListener(null, listener);
        return this;
    }

    /**
     * Registers a BusListener with a specific class filter.
     *
     * @param classFilter The object class to filter. Can be null to receive everything (any kind
     *      of object, including null) or the {@link Void} class to filter on message with null
     *      objects.
     * @param listener A receiver object implementing {@link com.alflabs.app.IBusListener}
     *                 or {@link com.alflabs.app.BusAdapter}.
     * @return self for chaining.
     */
    @NonNull
    public Bus register(@Null final Class<?> classFilter, final IBusListener listener) {
        _addListener(classFilter, listener);
        return this;
    }

    private void _addListener(@Null Class<?> filter, @NonNull IBusListener listener) {
        if (listener == null) return;

        List<WeakReference<IBusListener>> list = null;
        synchronized (mListeners) {
            list = mListeners.get(filter);
            if (list == null) {
                list = new LinkedList<WeakReference<IBusListener>>();
                mListeners.put(filter, list);
            }
        }
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (list) {
            boolean found = false;
            for (Iterator<WeakReference<IBusListener>> it = list.iterator(); it.hasNext(); ) {
                WeakReference<IBusListener> wr = it.next();
                if (wr.get() == null) {
                    it.remove();
                } else if (wr.get() == listener) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                WeakReference<IBusListener> ref = new WeakReference<IBusListener>(listener);
                list.add(ref);
            }
        }
    }

    /**
     * Unregisters the listener from the bus.
     *
     * @param listener A listener previously registered with the bus.
     * @return self for chaining.
     */
    @NonNull
    public Bus unregister(@NonNull IBusListener listener) {
        if (listener == null) return this;

        synchronized (mListeners) {
            for (List<WeakReference<IBusListener>> list : mListeners.values()) {
                for (Iterator<WeakReference<IBusListener>> it = list.iterator(); it.hasNext(); ) {
                    WeakReference<IBusListener> wr = it.next();
                    if (wr.get() == null) {
                        it.remove();
                    } else if (wr.get() == listener) {
                        it.remove();
                        break;
                    }
                }
            }
        }

        return this;
    }

    // -----

    /**
     * Returns a weak reference on the Bus that can be used to safely use the
     * Bus even if the Bus reference has been disposed.
     */
    @NonNull
    public Reference getReference() {
        return new Reference(this);
    }

    /**
     * Helper to safely use a weak bus reference.
     */
    public static class Reference {
        @NonNull
        private WeakReference<Bus> mBusRef;

        private Reference(@NonNull Bus bus) {
            mBusRef = new WeakReference<Bus>(bus);
        }

        /**
         * Returns the bus referenced. Might be null if the weak reference is gone.
         */
        @Null
        public Bus getBus() {
            return mBusRef.get();
        }

        /**
         * Registers a BusListener with no class filter.
         *
         * @param listener A receiver object implement {@link com.alflabs.app.IBusListener}
         *                 or {@link com.alflabs.app.BusAdapter}.
         * @return self for chaining.
         */
        @NonNull
        public Reference register(@NonNull IBusListener listener) {
            Bus b = getBus();
            if (b != null) {
                b.register(null, listener);
            }
            return this;
        }

        /**
         * Registers a BusListener with a specific class filter.
         *
         * @param classFilter The object class to filter. Can be null to receive everything (any kind
         *                    of object, including null) or the {@link Void} class to filter on message with null
         *                    objects.
         * @param listener    A receiver object implementing {@link IBusListener} or {@link BusAdapter}.
         * @return self for chaining.
         */
        @NonNull
        public Reference register(@Null Class<?> classFilter, @NonNull IBusListener listener) {
            Bus b = getBus();
            if (b != null) {
                b.register(classFilter, listener);
            }
            return this;
        }

        /**
         * Unregisters the listener from the bus.
         *
         * @param listener A listener previously registered with the bus.
         * @return self for chaining.
         */
        @NonNull
        public Reference unregister(@NonNull IBusListener listener) {
            Bus b = getBus();
            if (b != null) {
                b.unregister(listener);
            }
            return this;
        }

        /**
         * Returns a sender that can be used to safely send messages on the Bus
         * even if the Bus reference has been disposed.
         */
        @Null
        public Sender getSender() {
            Bus b = getBus();
            if (b != null) {
                return b.getSender();
            }
            return null;
        }

    }

    // -----

    /**
     * Returns a sender that can be used to safely send messages on the Bus
     * even if the Bus reference has been disposed.
     */
    @NonNull
    public Sender getSender() {
        return new Sender(this);
    }

    public static class Sender {
        @NonNull
        private WeakReference<Bus> mBusRef;

        private Sender(@NonNull Bus bus) {
            mBusRef = new WeakReference<Bus>(bus);
        }

        /**
         * Returns the bus referenced. Might be null if the weak reference is gone.
         */
        @Null
        public Bus getBus() {
            return mBusRef.get();
        }

        @NonNull
        public Sender safeSend(int what) {
            safeSend(what, null);
            return this;
        }

        @NonNull
        public Sender safeSend(@Null Object object) {
            safeSend(NO_WHAT, object);
            return this;
        }

        @NonNull
        public Sender safeSend(int what, @Null Object object) {
            if (mBusRef == null) {
                return this;
            }

            Bus bus = mBusRef.get();
            if (bus != null) {
                bus._send(what, object);
            }
            return this;
        }
    }

}


