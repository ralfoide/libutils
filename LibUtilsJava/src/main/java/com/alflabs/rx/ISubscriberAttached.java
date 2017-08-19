package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

/**
 * Optional decorator interface for {@link ISubscriber}, {@link IPublisher}, and {@link IProcessor}
 * indicating the object wants to be notified when a subscriber has been attached (added) or detached (removed)
 * from a stream.
 */
public interface ISubscriberAttached<Event> {
    /**
     * The {@link ISubscriber}, {@link IPublisher}, or {@link IProcessor} was attached (added) to the stream.
     */
    void onSubscriberAttached(@NonNull IStream<? super Event> stream, @NonNull ISubscriber<? super Event> subscriber);

    /**
     * The {@link ISubscriber}, {@link IPublisher}, or {@link IProcessor} was detached (removed) from the stream.
     */
    void onSubscriberDetached(@NonNull IStream<? super Event> stream, @NonNull ISubscriber<? super Event> subscriber);
}
