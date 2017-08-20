package com.alflabs.rx;

// Reminder about <? extends T> vs <? super T>:
// https://stackoverflow.com/questions/4343202: 'Remember PECS: "Producer Extends, Consumer Super".'
// e.g. class Foo extends Event extends Base
// for a Stream<Event>: Publisher can publish Foo or Event; Subscriber can receive Event or Base.

import com.alflabs.annotations.NonNull;

/**
 * A stream carries events created by publishers and sends them to subscribers.
 * A stream can have 0..N publishers and 0..M subscribers.
 * <p/>
 * A stream is either open, paused or closed. Closing the stream is a terminal action.
 * <p/>
 * A publisher is needed to publish events on a stream.
 */
public interface IStream<Event> extends _IPublishOnStream<Event> {

    /**
     * Sets the default scheduler on which the stream will operate.
     * Unless specified, publishers, subscribers and processors added after will also use that scheduler.
     */
    @NonNull
    IStream<Event> on(@NonNull IScheduler scheduler);

    /** Add a publisher to the stream. */
    @NonNull
    IStream<Event> publishWith(@NonNull IGenerator<? extends Event> publisher);

    /** Add a publisher to the stream, operating on the specified scheduler. */
    @NonNull
    IStream<Event> publishWith(@NonNull IGenerator<? extends Event> publisher, @NonNull IScheduler scheduler);

    /** Add a subscriber to the stream. */
    @NonNull
    IStream<Event> subscribe(@NonNull ISubscriber<? super Event> subscriber);

    /** Add a subscriber to the stream, operating on the specified scheduler. */
    @NonNull
    IStream<Event> subscribe(@NonNull ISubscriber<? super Event> subscriber, @NonNull IScheduler scheduler);

    /** Add a processor to the stream. */
    @NonNull
    <OutEvent> IStream<OutEvent> process(@NonNull IProcessor<? super Event, OutEvent> processor);

    /** Add a processor to the stream, operating on the specified scheduler. */
    @NonNull
    <OutEvent> IStream<OutEvent> process(@NonNull IProcessor<? super Event, OutEvent> processor, @NonNull IScheduler scheduler);

    /** Removes a previously added publisher. */
    @NonNull
    IStream<Event> remove(@NonNull IGenerator<? extends Event> publisher);

    /** Removes a previously added subscriber. */
    @NonNull
    IStream<Event> remove(@NonNull ISubscriber<? super Event> subscriber);

    /** Removes a previously added processor. */
    @NonNull
    <OutEvent> IStream<OutEvent> remove(@NonNull IProcessor<? super Event, OutEvent> processor);

    /** Returns the current state of the stream. */
    @NonNull
    State getState();

    /**
     * Sets the state of the stream.
     * <p/>
     * The {@link State#CLOSED} state is special: once closed, the stream state cannot change anymore.
     * <p/>
     * Clients cannot set the stream to the {@link State#IDLE} state -- this state is only reported when the stream
     * is open and has no subscribers. Trying to set the state to {@link State#IDLE} is treated as setting the
     * state to {@link State#OPEN}.
     *
     * @throws IllegalArgumentException when trying to set a closed stream to anything else than {@link State#CLOSED}.
     */
    @NonNull
    IStream<Event> setState(@NonNull State state);

    /** An alias for {@link #setState(State)} with {@link State#PAUSED}. */
    @NonNull
    IStream<Event> pause();

    /** An alias for {@link #setState(State)} with {@link State#OPEN} (to unpause a stream previously paused). */
    @NonNull
    IStream<Event> open();

    /** An alias for {@link #setState(State)} with {@link State#CLOSED}. */
    @NonNull
    IStream<Event> close();

    /** An alias for {@link #getState()} with {@link State#IDLE}. */
    boolean isIdle();

    /** An alias for {@link #getState()} with {@link State#OPEN}. */
    boolean isOpen();

    /** An alias for {@link #getState()} with {@link State#PAUSED}. */
    boolean isPaused();

    /** An alias for {@link #getState()} with {@link State#CLOSED}. */
    boolean isClosed();
}
