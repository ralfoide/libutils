package com.alflabs.rx.publishers;

import com.alflabs.annotations.NonNull;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStateChanged;
import com.alflabs.rx.IStream;
import com.alflabs.rx.State;

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

}
