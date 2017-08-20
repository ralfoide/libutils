package com.alflabs.rx.publishers;

import com.alflabs.rx.IPublish;
import com.alflabs.rx.IPublisher;

/**
 * Helper methods and classes for {@link IPublisher}.
 */
public class Publishers {

    /**
     * Returns a default publisher that just sends event into its stream.
     */
    public static <E> IPublish<E> publisher() {
        return new BasePublisher<>();
    }

    /**
     * Returns a simple publisher that publishes all the given values when first attached to a stream.
     */
    @SafeVarargs
    public static <E> IPublisher<E> just(E...values) {
        return new Just<E>(values);
    }

    /**
     * Returns a simple publisher that repeats the latest value when a new subscriber is added.
     */
    public static <E> IPublish<E> latest() {
        return new Latest<>();
    }
}
