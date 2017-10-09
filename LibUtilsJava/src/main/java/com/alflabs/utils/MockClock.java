package com.alflabs.utils;

public class MockClock implements IClock {
    public long mElapsedRealtime;
    public long mUptimeMillis;

    public MockClock() {
        mUptimeMillis = 1000;
        mElapsedRealtime = 2000;
    }

    @Override
    public long elapsedRealtime() {
        return mElapsedRealtime;
    }

    @Override
    public long uptimeMillis() {
        return mUptimeMillis;
    }

    @Override
    public void sleep(long ms) throws InterruptedException {
        mUptimeMillis += ms;
        mElapsedRealtime += ms;
    }
}
