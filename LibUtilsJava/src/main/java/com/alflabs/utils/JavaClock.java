package com.alflabs.utils;

public class JavaClock implements IClock {
    @Override
    public long elapsedRealtime() {
        return System.currentTimeMillis();
    }

    @Override
    public long uptimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}
