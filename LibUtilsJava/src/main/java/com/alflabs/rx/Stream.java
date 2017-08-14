package com.alflabs.rx;

import com.alflabs.func.RConsumer;

import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

class Stream<Event> implements IStream<Event> {

    private volatile State mState = State.IDLE;
    private volatile boolean mPaused;

    private final LinkedList<Event> mEvents = new LinkedList<>();
    private final Map<IPublisher, IScheduler> mPublishers = new ConcurrentHashMap<>();      // thread-safe
    private final Map<ISubscriber, IScheduler> mSubscribers = new ConcurrentHashMap<>();    // thread-safe
    private final Map<IProcessor, IScheduler> mProcessors = new ConcurrentHashMap<>();      // thread-safe
    private final IScheduler mSender = Schedulers.io();

    @Override
    public IStream<Event> publish(Event event) {
        if (mState != State.CLOSED) {
            synchronized (mEvents) {
                mEvents.addLast(event);
            }

            send();
        }
        return this;
    }

    @Override
    public IStream<Event> publishWith(IPublisher<? extends Event> publisher, IScheduler scheduler) {
        if (!mPublishers.containsKey(publisher)) {
            mPublishers.put(publisher, scheduler);
            scheduler.invoke(() -> publisher.onAttached(this));
        }
        return this;
    }

    @Override
    public IStream<Event> subscribe(ISubscriber<? super Event> subscriber, IScheduler scheduler) {
        mSubscribers.put(subscriber, scheduler);
        if (mState == State.IDLE) {
            changeState(State.OPEN);
        }
        return this;
    }

    @Override
    public <OutEvent> IStream<OutEvent> process(IProcessor<? super Event, OutEvent> processor, IScheduler scheduler) {
        mProcessors.put(processor, scheduler);
        return processor.output();
    }

    @Override
    public IStream<Event> remove(IPublisher<? extends Event> publisher) {
        IScheduler scheduler = mPublishers.get(publisher);
        if (scheduler != null) {
            mPublishers.remove(publisher);
            scheduler.invoke(() -> publisher.onDetached(this));
        }
        return this;
    }

    @Override
    public IStream<Event> remove(ISubscriber<? super Event> subscriber) {
        mSubscribers.remove(subscriber);
        if (mSubscribers.isEmpty() && mState == State.OPEN) {
            changeState(State.IDLE);
        }
        return this;
    }

    @Override
    public <OutEvent> IStream<OutEvent> remove(IProcessor<? super Event, OutEvent> processor) {
        mProcessors.remove(processor);
        return processor.output();
    }

    @Override
    public State state() {
        return mState;
    }

    @Override
    public IStream<Event> setState(State state) {
        if (state == State.IDLE) {
            state = State.OPEN;
        }

        if (mState == State.CLOSED && state != State.CLOSED) {
            throw new IllegalArgumentException("The stream is closed. It can't be set to " + state);
        }

        changeState(state);
        return this;
    }

    private void changeState(State newState) {
        State lastState = mState;
        mState = newState;

        if (mPaused && newState == State.OPEN) {
            mPaused = false;
        } else if (!mPaused && newState == State.PAUSED) {
            mPaused = true;
        }

        if (lastState != newState) {
            RConsumer<IStateChanged> consumer = object -> object.onStateChanged(this, newState);
            invokeAll(mSubscribers, consumer);
            invokeAll(mProcessors, consumer);
            invokeAll(mPublishers, consumer);
        }
    }

    private void send() {
        if (mState == State.PAUSED || mState == State.CLOSED || mEvents.isEmpty()) {
            return;
        }

        mSender.invoke(() -> {
            for(; !mEvents.isEmpty(); ) {
                if (mState == State.PAUSED || mState == State.CLOSED) {
                    return;
                }

                Event e;
                try {
                    synchronized (mEvents) {
                         e = mEvents.removeFirst();
                    }
                } catch (NoSuchElementException ignore) {
                    break;
                }

                if (!mSubscribers.isEmpty()) {
                    invokeAll(mSubscribers, subscriber -> subscriber.onReceive(this, e));
                }
                if (!mProcessors.isEmpty()) {
                    invokeAll(mProcessors, processor -> processor.process(e));
                }
            }
        });
    }

    private <T> void invokeAll(Map<? extends T, IScheduler> map, RConsumer<T> consumer) {
        if (map.isEmpty()) {
            return;
        }
        for (Map.Entry<? extends T, IScheduler> entry : map.entrySet()) {
            entry.getValue().invoke(consumer, entry.getKey());
        }
    }
}
