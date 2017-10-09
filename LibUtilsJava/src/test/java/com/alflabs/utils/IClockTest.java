package com.alflabs.utils;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class IClockTest {
    private MockClock mClock;

    @Before
    public void setUp() throws Exception {
        mClock = new MockClock();
    }

    @Test
    public void elapsedRealtime() throws Exception {
        assertThat(mClock.elapsedRealtime()).isGreaterThan(0L);

        mClock.mElapsedRealtime = 4200L;
        assertThat(mClock.elapsedRealtime()).isEqualTo(4200L);
    }

    @Test
    public void uptimeMillis() throws Exception {
        assertThat(mClock.uptimeMillis()).isGreaterThan(0L);

        mClock.mUptimeMillis = 4400L;
        assertThat(mClock.uptimeMillis()).isEqualTo(4400L);
    }

    @Test
    public void sleep() throws Exception {
        long now1 = mClock.elapsedRealtime();
        long now2 = mClock.uptimeMillis();

        mClock.sleep(1500L);

        assertThat(mClock.elapsedRealtime()).isEqualTo(now1 + 1500L);
        assertThat(mClock.uptimeMillis()).isEqualTo(now2 + 1500L);
    }

}
