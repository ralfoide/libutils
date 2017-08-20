package com.alflabs.rx.publishers;

import com.alflabs.annotations.NonNull;
import com.alflabs.rx.IAttached;
import com.alflabs.rx.IPublish;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStream;

/**
 * A simple publisher that publishes all the constructor values when first attached to a stream.
 */
class Just<E> implements IPublisher<E>, IAttached<E> {
    private final E[] mValues;

    @SafeVarargs
    public Just(E...values) {
        mValues = values;
    }

    @Override
    public void onAttached(@NonNull IStream<? super E> stream) {
        if (stream instanceof IPublish) {
            for (E value : mValues) {
                //noinspection unchecked
                ((IPublish) stream).publish(value);
            }
        }
    }

    @Override
    public void onDetached(@NonNull IStream<? super E> stream) {}
}
