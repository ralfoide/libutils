package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

/**
 * A subscriber adapter that provides default implementations to all the methods from
 * {@link ISubscriber}, including those from the optional {@link IStateChanged} and {@link IAttached} interfaces.
 * <p/>
 * It's probably overkill and has not much application outside of convenience for testing
 * and thus has been moved to the test package.
 */
class _SubAdapter<Event> implements ISubscriber<Event>, IStateChanged<Event>, IAttached<Event> {
    @Override
    public void onStateChanged(@NonNull IStream<? super Event> stream, @NonNull State newState) {}

    @Override
    public void onReceive(@NonNull IStream<? extends Event> stream, @Null Event event) {}

    @Override
    public void onAttached(@NonNull IStream<? super Event> stream) {}

    @Override
    public void onDetached(@NonNull IStream<? super Event> stream) {}
}
