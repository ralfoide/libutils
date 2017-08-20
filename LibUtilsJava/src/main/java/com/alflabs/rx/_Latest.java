package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

/**
 * A simple publisher that repeats the latest value when a new subscriber is added.
 * <p/>
 * This particular publisher does not publish null events.
 */
class _Latest<E> extends BasePublisher<E> implements ISubscriberAttached<E> {

    private E mLastEvent;

    @NonNull
    public IPublisher<E> publish(@Null E event) {
        mLastEvent = event;
        if (event != null) {
            IStream<? super E> stream = getStream();
            if (stream != null && stream.isOpen()) {
                super.publish(event);
            }
        }
        return this;
    }

    @Override
    public void onSubscriberAttached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {
        if (mLastEvent != null) {
            super.publish(mLastEvent);
        }
    }

    @Override
    public void onSubscriberDetached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {}
}
