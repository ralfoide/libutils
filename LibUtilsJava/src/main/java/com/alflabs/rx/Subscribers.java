package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

public class Subscribers {

    public static class Adapter<Event> implements ISubscriber<Event> {
        @Override
        public void onStateChanged(@NonNull IStream<? super Event> stream, @NonNull State newState) {}

        @Override
        public void onReceive(@NonNull IStream<? extends Event> stream, @Null Event event) {}
    }
}
