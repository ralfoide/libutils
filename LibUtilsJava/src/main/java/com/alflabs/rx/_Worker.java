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

package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.func.RConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Scheduler that uses a pool of cached worker threads to execute operations. */
class _Worker implements IScheduler {
    private static ExecutorService sWorkerPool = Executors.newCachedThreadPool();

    @Override
    public void invoke(@NonNull Runnable runnable) {
        sWorkerPool.execute(runnable);
    }

    @Override
    public <T> void invoke(@NonNull RConsumer<? super T> consumer, @Null T value) {
        sWorkerPool.execute(() -> consumer.accept(value));
    }
}
