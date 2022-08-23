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

import java.util.ArrayList;

import static com.google.common.truth.Truth.assertThat;

public class FakeClockTest {

    private FakeClock mClock;

    @Before
    public void setUp() throws Exception {
        mClock = new FakeClock(1000);
    }

    @Test
    public void testSetNow() {
        assertThat(mClock.elapsedRealtime()).isEqualTo(1000);

        mClock.setNow(2000);
        assertThat(mClock.elapsedRealtime()).isEqualTo(2000);
    }

    @Test
    public void testAdd() {
        assertThat(mClock.elapsedRealtime()).isEqualTo(1000);

        mClock.add(2000);
        assertThat(mClock.elapsedRealtime()).isEqualTo(3000);
    }

    @Test
    public void testSleep() {
        assertThat(mClock.elapsedRealtime()).isEqualTo(1000);

        mClock.sleep(2000);
        assertThat(mClock.elapsedRealtime()).isEqualTo(3000);

        mClock.sleep(-500);
        assertThat(mClock.elapsedRealtime()).isEqualTo(3000);
    }

    @Test
    public void testSleepCallback() {
        ArrayList<Long> invocations = new ArrayList<>();

        assertThat(mClock.elapsedRealtime()).isEqualTo(1000);

        mClock.sleep(500);

        mClock.setSleepCallback(invocations::add);
        mClock.sleep(600);
        mClock.sleep(400);

        mClock.setSleepCallback(null);
        mClock.sleep(1000);
        assertThat(invocations.toArray()).isEqualTo(new Object[] { 600L, 400L });
        assertThat(mClock.elapsedRealtime()).isEqualTo(3500);
    }

}
