package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

/**
 * A default publisher that just sends events into its stream.
 * <p/>
 * A publisher can only be attached to a single stream at once.
 * Before attaching to a new stream, it must be detached from the previous stream.
 * <p/>
 * This is a perfect base class for custom publishers.
 */
public class BasePublisher<E> extends BaseGenerator<E> implements IPublisher<E> {

    @NonNull
    public IPublisher<E> publish(@Null E event) {
        super.publishOnStream(event);
        return this;
    }
}
