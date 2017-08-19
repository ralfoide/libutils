package com.alflabs.rx.streams;

import com.alflabs.annotations.NonNull;
import com.alflabs.func.RConsumer;
import com.alflabs.rx.IAttached;
import com.alflabs.rx.IProcessor;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IScheduler;
import com.alflabs.rx.IStateChanged;
import com.alflabs.rx.IStream;
import com.alflabs.rx.ISubscriber;
import com.alflabs.rx.State;

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
    private IScheduler mScheduler;

    public Stream(@NonNull IScheduler scheduler) {
        mScheduler = scheduler;
    }

    @NonNull
    @Override
    public IStream<Event> on(@NonNull IScheduler scheduler) {
        mScheduler = scheduler;
        return this;
    }

    @NonNull
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

    @NonNull
    @Override
    public IStream<Event> publishWith(@NonNull IPublisher<? extends Event> publisher, @NonNull IScheduler scheduler) {
        if (!mPublishers.containsKey(publisher)) {
            mPublishers.put(publisher, scheduler);

            scheduler.invoke(() -> publisher.onAttached(this));
        }
        return this;
    }

    @NonNull
    @Override
    public IStream<Event> publishWith(@NonNull IPublisher<? extends Event> publisher) {
        return publishWith(publisher, mScheduler);
    }

    @NonNull
    @Override
    public IStream<Event> subscribe(@NonNull ISubscriber<? super Event> subscriber, @NonNull IScheduler scheduler) {
        if (!mSubscribers.containsKey(subscriber)) {
            mSubscribers.put(subscriber, scheduler);

            boolean doSend = false;
            if (mState == State.IDLE) {
                changeState(State.OPEN);
                doSend = true;
            }
            if (subscriber instanceof IAttached) {
                //noinspection unchecked
                scheduler.invoke(() -> ((IAttached) subscriber).onAttached(this));
            }
            if (doSend) {
                send();
            }
        }
        return this;
    }

    @NonNull
    @Override
    public IStream<Event> subscribe(@NonNull ISubscriber<? super Event> subscriber) {
        return subscribe(subscriber, mScheduler);
    }

    @NonNull
    @Override
    public <OutEvent> IStream<OutEvent> process(@NonNull IProcessor<? super Event, OutEvent> processor, @NonNull IScheduler scheduler) {
        if (!mProcessors.containsKey(processor)) {
            mProcessors.put(processor, scheduler);

            if (processor instanceof IAttached) {
                //noinspection unchecked
                scheduler.invoke(() -> ((IAttached) processor).onAttached(this));
            }
        }
        return processor.output();
    }

    @NonNull
    @Override
    public <OutEvent> IStream<OutEvent> process(@NonNull IProcessor<? super Event, OutEvent> processor) {
        return process(processor, mScheduler);
    }

    @NonNull
    @Override
    public IStream<Event> remove(@NonNull IPublisher<? extends Event> publisher) {
        IScheduler scheduler = mPublishers.get(publisher);
        if (scheduler != null) {
            mPublishers.remove(publisher);
            scheduler.invoke(() -> publisher.onDetached(this));
        }
        return this;
    }

    @NonNull
    @Override
    public IStream<Event> remove(@NonNull ISubscriber<? super Event> subscriber) {
        IScheduler scheduler = mSubscribers.get(subscriber);
        if (scheduler != null) {
            mSubscribers.remove(subscriber);

            if (mSubscribers.isEmpty() && mState == State.OPEN) {
                changeState(State.IDLE);
            }
            if (subscriber instanceof IAttached) {
                //noinspection unchecked
                scheduler.invoke(() -> ((IAttached) subscriber).onDetached(this));
            }
        }
        return this;
    }

    @NonNull
    @Override
    public <OutEvent> IStream<OutEvent> remove(@NonNull IProcessor<? super Event, OutEvent> processor) {
        IScheduler scheduler = mProcessors.get(processor);
        if (scheduler != null) {
            mProcessors.remove(processor);

            if (processor instanceof IAttached) {
                //noinspection unchecked
                scheduler.invoke(() -> ((IAttached) processor).onDetached(this));
            }
        }
        return processor.output();
    }

    @NonNull
    @Override
    public State getState() {
        return mState;
    }

    @NonNull
    @Override
    public IStream<Event> setState(@NonNull State state) {
        if (state == State.IDLE) {
            state = State.OPEN;
        }

        if (mState == State.CLOSED && state != State.CLOSED) {
            throw new IllegalArgumentException("The stream is closed. It can't be set to " + state);
        }

        changeState(state);
        return this;
    }

    @NonNull
    @Override
    public IStream<Event> pause() {
        return setState(State.PAUSED);
    }

    @NonNull
    @Override
    public IStream<Event> open() {
        return setState(State.OPEN);
    }

    @NonNull
    @Override
    public IStream<Event> close() {
        return setState(State.CLOSED);
    }

    @Override
    public boolean isIdle() {
        return mState == State.IDLE;
    }

    @Override
    public boolean isPaused() {
        return mState == State.PAUSED;
    }

    @Override
    public boolean isClosed() {
        return mState == State.CLOSED;
    }

    private void changeState(@NonNull State newState) {
        State lastState = mState;
        mState = newState;

        if (mPaused && newState == State.OPEN) {
            mPaused = false;
        } else if (!mPaused && newState == State.PAUSED) {
            mPaused = true;
        }

        if (lastState != newState) {
            RConsumer consumer = object -> {
                if (object instanceof IStateChanged) {
                    //noinspection unchecked
                    ((IStateChanged) object).onStateChanged(Stream.this, newState);
                }
            };
            //noinspection unchecked
            invokeAll(mSubscribers, consumer);
            //noinspection unchecked
            invokeAll(mProcessors, consumer);
            //noinspection unchecked
            invokeAll(mPublishers, consumer);
        }
    }

    private void send() {
        if (mState == State.CLOSED || mEvents.isEmpty()) {
            return;
        }

        mScheduler.invoke(() -> {
            for(; !mEvents.isEmpty(); ) {
                if (mState == State.IDLE || mState == State.PAUSED || mState == State.CLOSED) {
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
                    //noinspection unchecked
                    invokeAll(mSubscribers, subscriber -> subscriber.onReceive(this, e));
                }
                if (!mProcessors.isEmpty()) {
                    //noinspection unchecked
                    invokeAll(mProcessors, processor -> processor.process(e));
                }
            }
        });
    }

    private <T> void invokeAll(@NonNull Map<? extends T, IScheduler> map, @NonNull RConsumer<T> consumer) {
        if (map.isEmpty()) {
            return;
        }
        for (Map.Entry<? extends T, IScheduler> entry : map.entrySet()) {
            entry.getValue().invoke(consumer, entry.getKey());
        }
    }
}
