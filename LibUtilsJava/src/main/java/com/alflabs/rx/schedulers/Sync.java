package com.alflabs.rx.schedulers;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.func.RConsumer;
import com.alflabs.rx.IScheduler;

class Sync implements IScheduler {
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
