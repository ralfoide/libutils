package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.func.RConsumer;

interface IScheduler {
    void invoke(@NonNull Runnable runnable);
    <T> void invoke(@NonNull RConsumer<? super T> consumer, @Null T value);
}
