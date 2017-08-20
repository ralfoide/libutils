package com.alflabs.rx;

/**
 * Helper methods and classes for {@link IGenerator}.
 */
public class Publishers {

    /**
     * Returns a default publisher that just sends event into its stream.
     */
    public static <E> IPublisher<E> publisher() {
        return new BasePublisher<>();
    }

    /**
     * Returns a simple publisher that publishes all the given values when first attached to a stream.
     */
    @SafeVarargs
    public static <E> IGenerator<E> just(E...values) {
        return new _Just<E>(values);
    }

    /**
     * Returns a simple publisher that repeats the latest value when a new subscriber is added.
     */
    public static <E> IPublisher<E> latest() {
        return new _Latest<>();
    }
}
