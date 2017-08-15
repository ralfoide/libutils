package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

interface IStateChanged<Event> {
    /** Notified when the stream state has changed (paused or closed). */
    void onStateChanged(@NonNull IStream<? super Event> stream, @NonNull State newState);
}
