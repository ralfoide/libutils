/*
 * Project: LibUtils
 * Copyright (C) 2021 alf.labs gmail com,
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

import com.google.common.base.Strings;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class ThreadLoop implements IStartStop {
    protected Thread mThread;
    protected volatile boolean mQuit;
    private final CountDownLatch mLatchEndLoop = new CountDownLatch(1);

    @Override
    public void start() throws Exception {
        this.start("" /* default thread name */);
    }

    public void start(String name) throws Exception {
        if (mThread == null) {
            if (Strings.isNullOrEmpty(name)) {
                mThread = new Thread(this::_runInThread);
            } else {
                mThread = new Thread(this::_runInThread, name);
            }
            mQuit = false;
            mThread.start();
        }
    }

    /**
     * Stops the thread if running.
     * This sets the 'mQuit' flag, interrupts the thread, and joins it.
     */
    @Override
    public void stop() throws Exception {
        if (mThread != null) {
            Thread t = mThread;
            mThread = null;
            mQuit = true;
            t.interrupt();
            t.join();
        }
    }

    /**
     * Stops the thread if running but give the thread a preemtive warning with
     * a timeout to give it a chance to quit before being interrupted.
     * This sets the 'mQuit' flag, waits the given timeout, and only then interrupts the thread,
     * and joins it.
     * This gives a chance to the running thread to finish pending operations as soon as the
     * 'mQuit' flag is set and before the thread is interrupted. This can help finalize any pending
     * IOs instead of interrupting them immediately.
     */
    public void stopWithPreTimeout(long timeout, TimeUnit unit) throws Exception {
        if (mThread != null) {
            Thread t = mThread;
            mThread = null;
            mQuit = true;
            //noinspection ResultOfMethodCallIgnored
            mLatchEndLoop.await(timeout, unit);
            t.interrupt();
            t.join();
        }
    }

    private void _runInThread() {
        _beforeThreadLoop();
        try {
            while (!mQuit) {
                _runInThreadLoop();
            }
        } catch (EndLoopException ignored) {
            // No-logging, just end the loop.
        } catch (Throwable t) {
            System.out.println(
                    "ThreadLoop._runInThread [" + Thread.currentThread().getName() +
                            "] unhandled exception: " + t);
        } finally {
            _afterThreadLoop();
            mLatchEndLoop.countDown();
        }
    }

    public static class EndLoopException extends Exception {}

    /** Called once before the first {@code _runInThreadLoop} call. */
    protected void _beforeThreadLoop() {}

    /** Called in a loop as long as {@code mQuit} is false. */
    protected abstract void _runInThreadLoop() throws EndLoopException;

    /** Called once after the last {@code _runInThreadLoop} call. */
    protected void _afterThreadLoop() {}
}
