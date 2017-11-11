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
