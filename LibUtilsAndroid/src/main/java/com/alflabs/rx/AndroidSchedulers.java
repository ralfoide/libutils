package com.alflabs.rx;

import android.os.Handler;
import com.alflabs.annotations.NonNull;
import com.alflabs.func.RConsumer;

public class AndroidSchedulers {

    private static MainThreadHandler sHandler;

    /** Scheduler that executes operations on the Android main thread. */
    public static IScheduler mainThread() {
        if (sHandler == null) {
            sHandler = new MainThreadHandler();
        }

        return sHandler;
    }

    static class MainThreadHandler implements IScheduler {
        private Handler mHandler = new Handler();

        @Override
        public void invoke(@NonNull Runnable runnable) {
            mHandler.post(runnable);
        }

        @Override
        public <T> void invoke(@NonNull RConsumer<? super T> consumer, T value) {
            mHandler.post(() -> consumer.accept(value));
        }
    }
}
