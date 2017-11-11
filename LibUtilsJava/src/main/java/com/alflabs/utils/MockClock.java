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
