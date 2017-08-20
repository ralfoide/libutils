package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

/**
 * A simple publisher that repeats the latest value when a new subscriber is added.
 */
class _Latest<E> extends BasePublisher<E> implements ISubscriberAttached<E> {

    private E mLastEvent;

    @NonNull
    public IPublish<E> publish(@Null E event) {
        mLastEvent = event;
        for (IStream<? super E> stream : getStreams()) {
            if (stream.isOpen()) {
                stream._publishOnStream(event);
            }
        }
        return this;
    }

    @Override
    public void onSubscriberAttached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {
        if (mLastEvent != null) {
            stream._publishOnStream(mLastEvent);
        }
    }

    @Override
    public void onSubscriberDetached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {}
}
