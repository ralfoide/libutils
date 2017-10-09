package com.alflabs.utils;

public interface IClock {

    /**
     * Returns {@code SystemClock#elapsedRealtime()}:
     * On Android, returns milliseconds since boot, including time spent in sleep.
     */
    long elapsedRealtime();

    /**
     * Returns {@code SystemClock#uptimeMillis()}:
     * On Android, returns milliseconds since boot, not counting time spent in deep sleep.
     */
    long uptimeMillis();

    /**
     * Calls {@link Thread#sleep(long)}.
     *
     * @see Thread#sleep(long)
     */
    void sleep(long ms) throws InterruptedException;
}
