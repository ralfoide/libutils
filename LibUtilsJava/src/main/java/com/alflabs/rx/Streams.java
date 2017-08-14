package com.alflabs.rx;

public class Streams {

    public static <Event> IStream<Event> create() {
        return new Stream<Event>();
    }

}
