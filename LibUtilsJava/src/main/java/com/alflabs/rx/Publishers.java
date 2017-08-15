package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

public class Publishers {

    @SafeVarargs
    public static <E> IPublisher<E> just(E...values) {
        return new Just<E>(values);
    }

    public static class Adapter<E> implements IPublisher<E> {
        @Override
        public void onStateChanged(@NonNull IStream<? super E> stream, @NonNull State newState) {}

        @Override
        public void onAttached(@NonNull IStream<? super E> stream) {}

        @Override
        public void onDetached(@NonNull IStream<? super E> stream) {}
    }

    static class Just<E> extends Adapter<E> {
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
}
