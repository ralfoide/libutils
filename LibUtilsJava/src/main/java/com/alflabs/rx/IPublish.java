package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

/**
 * A publisher with a {@link #publish(Event)} method.
 */
public interface IPublish<Event> extends IPublisher<Event> {
    /**
     * Publishes a new event to the stream.
     * <p/>
     * Unless a specific stream implementation provides constraints, the publish method should be
     * treated as thread-safe and asynchronous.
     */
    @NonNull
    IPublish<Event> publish(Event event);
}
