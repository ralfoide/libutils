package com.alflabs.rx.subscribers;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.rx.IAttached;
import com.alflabs.rx.IStateChanged;
import com.alflabs.rx.IStream;
import com.alflabs.rx.ISubscriber;
import com.alflabs.rx.State;

/**
 * A subscriber adapter that provides default implementations to all the methods from
 * {@link ISubscriber}, including those from the optional {@link IStateChanged} and {@link IAttached} interfaces.
 */
public class SubAdapter<Event> implements ISubscriber<Event>, IStateChanged<Event>, IAttached<Event> {
    @Override
    public void onStateChanged(@NonNull IStream<? super Event> stream, @NonNull State newState) {}

    @Override
    public void onReceive(@NonNull IStream<? extends Event> stream, @Null Event event) {}

    @Override
    public void onAttached(@NonNull IStream<? super Event> stream) {}

    @Override
    public void onDetached(@NonNull IStream<? super Event> stream) {}
}
