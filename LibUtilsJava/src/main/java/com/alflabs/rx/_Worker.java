package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.func.RConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
