package com.alflabs.rx.publishers;

import com.alflabs.annotations.NonNull;
import com.alflabs.rx.IStream;

class Just<E> extends Adapter<E> {
    private final E[] mValues;

    @SafeVarargs
    public Just(E...values) {
        mValues = values;
    }

    @Override
    public void onAttached(@NonNull IStream<? super E> stream) {
        for (E value : mValues) {
            stream.publish(value);
        }
    }
}
