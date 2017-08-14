package com.alflabs.rx;

interface IStateChanged<Event> {
    /** Notified when the stream state has changed (paused or closed). */
    void onStateChanged(IStream<? super Event> stream, State newState);
}
