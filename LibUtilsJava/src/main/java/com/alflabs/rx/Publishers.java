package com.alflabs.rx;

public class Publishers {

    public static <E> IPublisher<E> just(E...values) {
        return new Just<E>(values);
    }

    public static class Adapter<E> implements IPublisher<E> {
        @Override
        public void onStateChanged(IStream<? super E> stream, State newState) {}

        @Override
        public void onAttached(IStream<? super E> stream) {}

        @Override
        public void onDetached(IStream<? super E> stream) {}
    }

    static class Just<E> extends Adapter<E> {
        private final E[] mValues;

        public Just(E...values) {
            mValues = values;
        }

        @Override
        public void onAttached(IStream<? super E> stream) {
            for (E value : mValues) {
                stream.publish(value);
            }
        }
    }
}
