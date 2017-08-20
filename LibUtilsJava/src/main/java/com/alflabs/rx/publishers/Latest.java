package com.alflabs.rx.publishers;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.rx.IAttached;
import com.alflabs.rx.IPublish;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStream;
import com.alflabs.rx.ISubscriber;
import com.alflabs.rx.ISubscriberAttached;
import com.alflabs.rx.State;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple publisher that repeats the latest value when a new subscriber is added.
 */
class Latest<E> implements IPublish<E>, IPublisher<E>, IAttached<E>, ISubscriberAttached<E> {

    private final Map<IStream<? super E>, Boolean> mStreams = new ConcurrentHashMap<>(1, 0.75f, 1);    // thread-safe
    private E mLastEvent;

    @NonNull
    public IPublish<E> publish(@Null E event) {
        mLastEvent = event;
        sendAll();
        return this;
    }

    @Override
    public void onSubscriberAttached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {
        if (mLastEvent != null) {
            send(stream);
        }
    }

    @Override
    public void onSubscriberDetached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {
        // no-op
    }

    @Override
    public void onAttached(@NonNull IStream<? super E> stream) {
        mStreams.put(stream, Boolean.TRUE);
    }

    @Override
    public void onDetached(@NonNull IStream<? super E> stream) {
        mStreams.remove(stream);
    }

    private void send(@NonNull IStream<? super E> stream) {
        if (stream.getState() == State.OPEN) {
            stream.publish(mLastEvent);
        }
    }

    private void sendAll() {
        for (IStream<? super E> stream : mStreams.keySet()) {
            send(stream);
        }
    }
}
