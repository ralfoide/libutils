package com.alflabs.rx.streams;

import com.alflabs.annotations.NonNull;
import com.alflabs.rx.IScheduler;
import com.alflabs.rx.IStream;

/**
 * Helper methods and classes for {@link IStream}.
 */
public class Streams {

    /**
     * Creates a new {@link IStream} with no subscribers and no publishers.
     * <p/>
     * The stream is thread-safe. Adding or removing publishers and schedulers, as well as invoking
     * {@link IStream#publish} can be done from any thread. The stream implements a basic queue -- when paused,
     * all published events are queued and delivered when the stream is reopen. Similarly, any events queued
     * before the first publisher are delivered once the first subscriber or processor is attached.
     * <p/>
     * Publish calls are protected by a lock and events are guaranteed to be delivered in a FIFO order.
     * Subscribers are never called from within the publish lock and a subscriber can thus safely publish
     * onto the same stream than it receives.
     * <p/>
     * Changing the stream state is immediate. Consequently, calling publish on the io scheduler followed by
     * a pause/close call may result in the published event happen in any order with regard to the state change.
     * <p/>
     * The stream uses {@link com.alflabs.rx.schedulers.Schedulers#io()} by default unless changed by {@link IStream#on(IScheduler)}.
     */
    @NonNull
    public static <Event> IStream<Event> stream() {
        return new Stream<>(com.alflabs.rx.schedulers.Schedulers.io());
    }
}
