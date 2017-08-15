package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

/**
 * A publisher is a generator that publishes events to a stream.
 * <p/>
 * Publishers are convenience objects. Anything can use {@link IStream#publish(Object)} directly.
 * <p/>
 * A publishers is generally a synchronous or asynchronous objects that generates one or more event
 * and publishes them directly to the underlying stream when attached to it.
 * <p/>
 * Publishers are attached to a single stream and operate on the scheduler indicated when attached.
 * <p/>
 * Optional interface: if the publisher implements {@link IStateChanged<Event>}, it will be notified
 * when the stream changes state.
 */
public interface IPublisher<Event> {

    void onAttached(@NonNull IStream<? super Event> stream);
    void onDetached(@NonNull IStream<? super Event> stream);
}
