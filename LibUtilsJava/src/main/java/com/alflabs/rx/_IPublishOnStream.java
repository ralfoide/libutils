package com.alflabs.rx;

/**
 * A stream with a {@link #_publishOnStream(Event)} method.
 */
interface _IPublishOnStream<Event> {
    /**
     * Publishes a new event to a stream.
     * <p/>
     * Unless a specific stream implementation provides constraints, the publish method should be
     * treated as thread-safe and asynchronous.
     */
    void _publishOnStream(Event event);
}
