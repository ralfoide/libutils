package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.func.RConsumer;

class _Sync implements IScheduler {
    @Override
    public void invoke(@NonNull Runnable runnable) {
        runnable.run();
    }

    @Override
    public <T> void invoke(@NonNull RConsumer<? super T> consumer, @Null T value) {
        consumer.accept(value);
    }
}
