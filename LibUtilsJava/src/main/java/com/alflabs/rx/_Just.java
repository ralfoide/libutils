package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

/**
 * A simple publisher that publishes all the constructor values when first attached to a stream.
 */
class _Just<E> implements IPublisher<E>, IAttached<E> {
    private final E[] mValues;

    @SafeVarargs
    public _Just(E...values) {
        mValues = values;
    }

    @Override
    public void onAttached(@NonNull IStream<? super E> stream) {
        for (E value : mValues) {
            stream._publishOnStream(value);
        }
    }

    @Override
    public void onDetached(@NonNull IStream<? super E> stream) {}
}
