package com.alflabs.rx;

import com.alflabs.func.RConsumer;

interface IScheduler {
    void invoke(Runnable runnable);
    <T> void invoke(RConsumer<? super T> consumer, T value);
}
