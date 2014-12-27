/*
 * (c) ralfoide gmail com, 2008
 * Project: TimerApp
 * License TBD
 */

package com.alflabs.app;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------

/**
 * A {@link Bus} is a a simplistic message-based notifier.
 * <p/>
 * The Buses classes manages the global list of buses.
 * Each bus has an id so it's possible to find a bus by its
 * id if the referenec is still alive.
 */
public final class Buses {

    private static final String TAG = Buses.class.getSimpleName();
    private static final boolean DEBUG = Utils.isEmulator();

    private static final List<WeakReference<Bus>> sBuses = new ArrayList<WeakReference<Bus>>();
    private static int mNextId = 0;

    private Buses() {
    }

    /**
     * Creates a new bus. The {@link Buses} only keeps a weak referenec onto the
     * bus so it's up to the caller to hold the bus object.
     */
    @NonNull
    public static Bus newBus() {
        synchronized (sBuses) {
            Bus b = new Bus(++mNextId);
            sBuses.add(new WeakReference<Bus>(b));
            return b;
        }
    }

    @Null
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
}


