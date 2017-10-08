package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.func.RConsumer;

/** Scheduler that makes direct immediate calls to execute operations. */
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
