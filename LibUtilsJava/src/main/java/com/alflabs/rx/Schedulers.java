package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.func.RConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Schedulers {
    private static ExecutorService sWorkerPool = Executors.newCachedThreadPool();
    private static Sync sSync;
    private static Worker sWorker;

    @NonNull
    public static IScheduler sync() {
        if (sSync == null) {
            sSync = new Sync();
        }
        return sSync;
    }

    @NonNull
    public static IScheduler io() {
        if (sWorker == null) {
            sWorker = new Worker();
        }
        return sWorker;
    }

    static class Worker implements IScheduler {
        @Override
        public void invoke(@NonNull Runnable runnable) {
            sWorkerPool.execute(runnable);
        }

        @Override
        public <T> void invoke(@NonNull RConsumer<? super T> consumer, @Null T value) {
            sWorkerPool.execute(() -> consumer.accept(value));
        }
    }

    static class Sync implements IScheduler {

        @Override
        public void invoke(@NonNull Runnable runnable) {
            try {
                runnable.run();
            } catch (Throwable e) {
                System.out.println(e.toString());
            }
        }

        @Override
        public <T> void invoke(@NonNull RConsumer<? super T> consumer, @Null T value) {
            try {
                consumer.accept(value);
            } catch (Throwable e) {
                System.out.println(e.toString());
            }
        }
    }
}
