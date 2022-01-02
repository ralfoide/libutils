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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class FakeClock implements IClock {
    private long mNow;
    private final AtomicReference<Consumer<Long>> mSleepCallback = new AtomicReference<>(null);

    public FakeClock(long now) {
        mNow = now;
    }

    public void setNow(long now) {
        mNow = now;
    }

    public void add(long now) {
        mNow += now;
    }

    @Override
    public long elapsedRealtime() {
        return mNow;
    }

    @Override
    public long uptimeMillis() {
        return mNow;
    }

    @Override
    public void sleep(long sleepTimeMs) {
        if (sleepTimeMs > 0) {
            add(sleepTimeMs);
        }
        Consumer<Long> callback = mSleepCallback.get();
        if (callback != null) {
            callback.accept(sleepTimeMs);
        }
    }

    @Override
    public void sleepWithInterrupt(long sleepTimeMs) throws InterruptedException {
        sleep(sleepTimeMs);
    }

    /**
     * Sets a callback to be called when {@link #sleep(long)} or {@link #sleepWithInterrupt(long)}
     * are invoked. The argument to the callback is the sleepTimeMs. Use null to remove any
     * current callback.
     */
    public void setSleepCallback(Consumer<Long> sleepCallback) {
        mSleepCallback.set(sleepCallback);
    }
}
