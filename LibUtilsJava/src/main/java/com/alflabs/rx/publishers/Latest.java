package com.alflabs.rx.publishers;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.rx.IPublish;
import com.alflabs.rx.IStream;
import com.alflabs.rx.ISubscriber;
import com.alflabs.rx.ISubscriberAttached;

/**
 * A simple publisher that repeats the latest value when a new subscriber is added.
 */
class Latest<E> extends BasePublisher<E> implements ISubscriberAttached<E> {

    private E mLastEvent;

    @NonNull
    public IPublish<E> publish(@Null E event) {
        mLastEvent = event;
        super.publish(event);
        return this;
    }

    @Override
    public void onSubscriberAttached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {
        if (mLastEvent != null) {
            publishOnStream(mLastEvent, stream);
        }
    }

    @Override
    public void onSubscriberDetached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {
        // no-op
    }
}
