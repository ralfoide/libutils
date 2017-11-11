/*
 * Project: Lib Utils
 * Copyright (C) 2008 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.alflabs.app.v1;

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
 * id if the reference is still alive.
 */
public class Buses {

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


