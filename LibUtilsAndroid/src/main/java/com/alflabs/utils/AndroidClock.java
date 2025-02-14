/*
 * Project: Lib Utils
 * Copyright (C) 2017 alf.labs gmail com,
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

package com.alflabs.utils;

import android.os.SystemClock;

public class AndroidClock implements IClock {

    /**
     * Returns {@link SystemClock#elapsedRealtime()}:
     * Returns milliseconds since boot, including time spent in sleep.
     *
     * @see SystemClock#elapsedRealtime()
     */
    @Override
    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    /**
     * Returns {@link SystemClock#uptimeMillis()}:
     * Returns milliseconds since boot, not counting time spent in deep sleep.
     *
     * @see SystemClock#uptimeMillis()
     */
    @Override
    public long uptimeMillis() {
        return SystemClock.uptimeMillis();
    }

    /**
     * Returns {@link SystemClock#elapsedRealtimeNanos()}:
     * Returns nanoseconds since boot, including time spent in sleep.
     *
     * @see SystemClock#elapsedRealtimeNanos()
     */
    @Override
    public long nanoTime() {
        return SystemClock.elapsedRealtimeNanos();
    }

    /**
     * Calls {@link Thread#sleep(long)}.
     *
     * @see Thread#sleep(long)
     */
    @Override
    public void sleep(long ms) {
        try {
            sleepWithInterrupt(ms);
        } catch (InterruptedException ignore) {}
    }

    /**
     * Calls {@link Thread#sleep(long)}.
     *
     * @see Thread#sleep(long)
     */
    @Override
    public void sleepWithInterrupt(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}
