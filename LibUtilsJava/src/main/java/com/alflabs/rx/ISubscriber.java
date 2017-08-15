package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

/**
 * A Subscriber is a reader, consumer, observer. It takes a stream and is notified when an event is published.
 * <p/>
 * To start receiving, a subscriber needs to be subscribed to a stream.
 * It will be invoked by the stream using the scheduler indicated when subscribing.
 * <p/>
 * The same subscriber can be subscribed to more than one stream, even on different schedulers.
 * <p/>
 * Optional interface: <br/>
 * - if the subscriber implements {@link IStateChanged<Event>}, it will be notified when the stream changes state. <br/>
 * - if the subscriber implements {@link IAttached<Event>}, it will be notified when attached to the stream. <br/>
 */
public interface ISubscriber<Event> {
    /** Receives an event that was published to the stream, if attached to one. */
    void onReceive(@NonNull IStream<? extends Event> stream, @Null Event event);
}
