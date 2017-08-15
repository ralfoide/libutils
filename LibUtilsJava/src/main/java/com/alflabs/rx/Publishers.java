package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

/**
 * Helper methods and classes for {@link IPublisher}.
 */
public class Publishers {

    /**
     * Returns a simple publisher that publishes all the given values when first attached to a stream.
     */
    @SafeVarargs
    public static <E> IPublisher<E> just(E...values) {
        return new Just<E>(values);
    }

    /**
     * A publisher adapter that provides default implementations to all the methods from
     * {@link IPublisher}, including those from the optional {@link IStateChanged} interface.
     */
    public static class Adapter<E> implements IPublisher<E>, IStateChanged<E> {
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
