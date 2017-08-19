package com.alflabs.rx.publishers;

import com.alflabs.annotations.NonNull;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStateChanged;
import com.alflabs.rx.IStream;
import com.alflabs.rx.ISubscriber;
import com.alflabs.rx.ISubscriberAttached;
import com.alflabs.rx.State;

/**
 * A publisher adapter that provides default implementations to all the methods from
 * {@link IPublisher}, including those from the optional {@link IStateChanged} interface.
 */
public class Adapter<E> implements IPublisher<E>, IStateChanged<E>, ISubscriberAttached<E> {
    @Override
    public void onStateChanged(@NonNull IStream<? super E> stream, @NonNull State newState) {}

    @Override
    public void onAttached(@NonNull IStream<? super E> stream) {}

    @Override
    public void onDetached(@NonNull IStream<? super E> stream) {}

    @Override
    public void onSubscriberAttached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {}

    @Override
    public void onSubscriberDetached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {}
}
