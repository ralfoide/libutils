package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

public class Streams {

    @NonNull
    public static <Event> IStream<Event> create() {
        return new Stream<Event>(Schedulers.io());
    }

}
