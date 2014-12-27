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
     * @param listener A receiver object implement {@link IBusListener} or {@link BusAdapter}.
     */
    public void register(IBusListener listener) {
        _addListener(null, listener);
    }

    /**
     * Registers a BusListener with a specific class filter.
     *
     * @param classFilter The object class to filter. Can be null to receive everything (any kind
     *      of object, including null) or the {@link Void} class to filter on message with null
     *      objects.
     * @param listener A receiver object implementing {@link IBusListener} or {@link BusAdapter}.
     */
    public void register(@Null final Class<?> classFilter, final IBusListener listener) {
        _addListener(classFilter, listener);
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

    public void unregister(@NonNull IBusListener listener) {
        if (listener == null) return;

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
    }

    // -----

    /**
     * Returns a weak reference on the Bus that can be used to safely use the
     * Bus even if the Bus reference has been disposed.
     */
    public Reference getReference() {
        return new Reference(this);
    }

    /**
     * Helper to safely use a weak bus reference.
     */
    public static class Reference {
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
         * @param listener A receiver object implement {@link IBusListener} or {@link BusAdapter}.
         * @return true if the Bus reference is still valid and the action was done.
         */
        public boolean register(@NonNull IBusListener listener) {
            Bus b = getBus();
            if (b != null) {
                b.register(null, listener);
                return true;
            }
            return false;
        }

        /**
         * Registers a BusListener with a specific class filter.
         *
         * @param classFilter The object class to filter. Can be null to receive everything (any kind
         *                    of object, including null) or the {@link Void} class to filter on message with null
         *                    objects.
         * @param listener    A receiver object implementing {@link com.alflabs.app.IBusListener} or {@link com.alflabs.app.BusAdapter}.
         * @return true if the Bus reference is still valid and the action was done.
         */
        public boolean register(@Null Class<?> classFilter, @NonNull IBusListener listener) {
            Bus b = getBus();
            if (b != null) {
                b.register(classFilter, listener);
                return true;
            }
            return false;
        }

        public boolean unregister(@NonNull IBusListener listener) {
            Bus b = getBus();
            if (b != null) {
                b.unregister(listener);
                return true;
            }
            return false;
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
    public Sender getSender() {
        return new Sender(this);
    }

    public static class Sender {
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

        public void safeSend(int what) {
            this.safeSend(what, null);
        }

        public void safeSend(@Null Object object) {
            this.safeSend(NO_WHAT, object);
        }

        public void safeSend(int what, @Null Object object) {
            if (mBusRef == null) {
                return;
            }

            Bus bus = mBusRef.get();
            if (bus != null) {
                bus._send(what, object);
            }
        }
    }

}


