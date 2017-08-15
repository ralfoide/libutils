package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

/**
 * Helper methods and classes for {@link ISubscriber}.
 */
public class Subscribers {

    /**
     * A subscriber adapter that provides default implementations to all the methods from
     * {@link ISubscriber}, including those from the optional {@link IStateChanged} interface.
     */
    public static class Adapter<Event> implements ISubscriber<Event>, IStateChanged<Event> {
        @Override
        public void onStateChanged(@NonNull IStream<? super Event> stream, @NonNull State newState) {}

        @Override
        public void onReceive(@NonNull IStream<? extends Event> stream, @Null Event event) {}
    }
}
