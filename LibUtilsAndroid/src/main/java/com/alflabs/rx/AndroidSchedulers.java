package com.alflabs.rx;

import android.os.Handler;
import com.alflabs.func.RConsumer;

public class AndroidSchedulers {

    private static MainThreadHandler sHandler;

    public static IScheduler mainThread() {
        if (sHandler == null) {
            sHandler = new MainThreadHandler();
        }

        return sHandler;
    }

    static class MainThreadHandler implements IScheduler {
        private Handler mHandler = new Handler();

        @Override
        public void invoke(Runnable runnable) {
            mHandler.post(runnable);
        }

        @Override
        public <T> void invoke(RConsumer<? super T> consumer, T value) {
            mHandler.post(() -> consumer.accept(value));
        }
    }
}
