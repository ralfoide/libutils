package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

/**
 * Optional decorator interface for {@link ISubscriber}, {@link IPublisher}, and {@link IProcessor}
 * indicating the object wants to be notified when the stream's {@link State} changes.
 */
public interface IStateChanged<Event> {
    /** Notified when the stream state has changed (paused or closed). */
    void onStateChanged(@NonNull IStream<? super Event> stream, @NonNull State newState);
}
