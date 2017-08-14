package com.alflabs.rx;

// Reminder about <? extends T> vs <? super T>:
// https://stackoverflow.com/questions/4343202: 'Remember PECS: "Producer Extends, Consumer Super".'
// e.g. class Foo extends Event extends Base
// for a Stream<Event>: Publisher can publish Foo or Event; Subscriber can receive Event or Base.

/**
 * A stream carries even created by publishers and sends them to subscribers.
 * A stream can have 0..N publishers and 0..M subscribers.
 * <p/>
 * A stream is either open, paused or closed. Closing the stream is a terminal action.
 * <p/>
 * Publishers are optional convenient generators.
 * Anything can directly publish to the stream without an actual publisher.
 */
public interface IStream<Event> {

    /**
     * Publishes a new event to the stream.
     * <p/>
     * Unless a specific stream implementation provides constraints, the publish method should be
     * treated as thread-safe and asynchronous.
     */
    IStream<Event> publish(Event event);

    /** Add a publisher to the stream. */
    IStream<Event> publishWith(IPublisher<? extends Event> publisher, IScheduler scheduler);

    /** Add a subscriber to the stream. */
    IStream<Event> subscribe(ISubscriber<? super Event> subscriber, IScheduler scheduler);

    /** Add a processor to the stream. */
    <OutEvent> IStream<OutEvent> process(IProcessor<? super Event, OutEvent> processor, IScheduler scheduler);

    /** Removes a previously added publisher. */
    IStream<Event> remove(IPublisher<? extends Event> publisher);

    /** Removes a previously added subscriber. */
    IStream<Event> remove(ISubscriber<? super Event> subscriber);

    /** Removes a previously added processor. */
    <OutEvent> IStream<OutEvent> remove(IProcessor<? super Event, OutEvent> processor);

    /** Returns the current state of the stream. */
    State state();

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
    IStream<Event> setState(State state);
}
