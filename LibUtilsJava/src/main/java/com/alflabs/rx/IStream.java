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
 * Publishers are optional convenient generators.
 * Any caller can directly {@link #publish(Object)} to the stream without an actual publisher.
 */
public interface IStream<Event> {

    /**
     * Sets the default scheduler on which the stream will operate.
     * Unless specified, publishers, subscribers and processors added after will also use that scheduler.
     */
    IStream<Event> on(@NonNull IScheduler scheduler);

    /**
     * Publishes a new event to the stream.
     * <p/>
     * Unless a specific stream implementation provides constraints, the publish method should be
     * treated as thread-safe and asynchronous.
     */
    IStream<Event> publish(Event event);

    /** Add a publisher to the stream. */
    IStream<Event> publishWith(@NonNull IPublisher<? extends Event> publisher);

    /** Add a publisher to the stream, operating on the specified scheduler. */
    IStream<Event> publishWith(@NonNull IPublisher<? extends Event> publisher, IScheduler scheduler);

    /** Add a subscriber to the stream. */
    IStream<Event> subscribe(@NonNull ISubscriber<? super Event> subscriber);

    /** Add a subscriber to the stream, operating on the specified scheduler. */
    IStream<Event> subscribe(@NonNull ISubscriber<? super Event> subscriber, @NonNull IScheduler scheduler);

    /** Add a processor to the stream. */
    <OutEvent> IStream<OutEvent> process(@NonNull IProcessor<? super Event, OutEvent> processor);

    /** Add a processor to the stream, operating on the specified scheduler. */
    <OutEvent> IStream<OutEvent> process(@NonNull IProcessor<? super Event, OutEvent> processor, @NonNull IScheduler scheduler);

    /** Removes a previously added publisher. */
    IStream<Event> remove(@NonNull IPublisher<? extends Event> publisher);

    /** Removes a previously added subscriber. */
    IStream<Event> remove(@NonNull ISubscriber<? super Event> subscriber);

    /** Removes a previously added processor. */
    <OutEvent> IStream<OutEvent> remove(@NonNull IProcessor<? super Event, OutEvent> processor);

    /** Returns the current state of the stream. */
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
    IStream<Event> setState(@NonNull State state);

    /** An alias for {@link #setState(State)} with {@link State#PAUSED}. */
    IStream<Event> pause();

    /** An alias for {@link #setState(State)} with {@link State#OPEN} (to unpause a stream previously paused). */
    IStream<Event> open();

    /** An alias for {@link #setState(State)} with {@link State#CLOSED}. */
    IStream<Event> close();

    /** An alias for {@link #getState()} with {@link State#IDLE}. */
    boolean isIdle();

    /** An alias for {@link #getState()} with {@link State#PAUSED}. */
    boolean isPaused();

    /** An alias for {@link #getState()} with {@link State#CLOSED}. */
    boolean isClosed();
}
