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
     * Calls {@link Thread#sleep(long)}.
     *
     * @see Thread#sleep(long)
     */
    @Override
    public void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}
