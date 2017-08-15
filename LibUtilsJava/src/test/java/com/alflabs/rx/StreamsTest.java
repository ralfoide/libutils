package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class StreamsTest {
    @Rule public MockitoRule mRule = MockitoJUnit.rule();

    @Mock Subscribers.Adapter<Integer> mIntSubscriber;

    @Test
    public void testStreamPublish1() throws Exception {
        AtomicInteger result = new AtomicInteger(0);

        Streams.<Integer>create()
                .on(Schedulers.sync())
                .subscribe((stream, integer) -> result.set(integer)).publish(42)
                .close();
        assertThat(result.get()).isEqualTo(42);
    }

    @Test
    public void testStreamPublish2() throws Exception {
        AtomicInteger result = new AtomicInteger(0);
        ISubscriber<Integer> subscriber = (stream, integer) -> result.set(integer);

        IStream<Integer> stream = Streams.<Integer>create().on(Schedulers.sync());
        assertThat(stream.state()).isEqualTo(State.IDLE);

        stream.subscribe(subscriber, Schedulers.sync());
        assertThat(stream.state()).isEqualTo(State.OPEN);

        stream.publish(42);
        stream.close();
        assertThat(result.get()).isEqualTo(42);
    }

    @Test
    public void testStreamPublish3() throws Exception {
        IStream<Integer> stream = Streams.<Integer>create()
                .on(Schedulers.sync())
                .subscribe(mIntSubscriber)
                .publish(42)
                .publish(43)
                .close();

        verify(mIntSubscriber).onAttached(stream);
        verify(mIntSubscriber).onStateChanged(stream, State.OPEN);
        verify(mIntSubscriber).onReceive(stream, 42);
        verify(mIntSubscriber).onReceive(stream, 43);
        verify(mIntSubscriber).onStateChanged(stream, State.CLOSED);
        verifyNoMoreInteractions(mIntSubscriber);
    }

    @Test
    public void testStreamPause() throws Exception {
        IStream<Integer> stream = Streams.<Integer>create()
                .on(Schedulers.sync())
                .subscribe(mIntSubscriber)
                .publish(42)
                .publish(43)
                .pause()
                .publish(44)
                .publish(45);

        verify(mIntSubscriber).onAttached(stream);
        verify(mIntSubscriber).onStateChanged(stream, State.OPEN);
        verify(mIntSubscriber).onReceive(stream, 42);
        verify(mIntSubscriber).onReceive(stream, 43);
        verify(mIntSubscriber).onStateChanged(stream, State.PAUSED);

        verify(mIntSubscriber, never()).onReceive(stream, 44);
        verify(mIntSubscriber, never()).onReceive(stream, 45);
        verify(mIntSubscriber, never()).onStateChanged(stream, State.CLOSED);

        stream.open();
        verify(mIntSubscriber, times(2)).onStateChanged(stream, State.OPEN);
        verify(mIntSubscriber, never()).onReceive(stream, 44);
        verify(mIntSubscriber, never()).onReceive(stream, 45);

        stream.close();
        verify(mIntSubscriber).onStateChanged(stream, State.CLOSED);

        verifyNoMoreInteractions(mIntSubscriber);
    }

    @Test
    public void testStreamAttachedDetached() throws Exception {

        IStream<Integer> stream = Streams.<Integer>create().on(Schedulers.sync());
        verify(mIntSubscriber, never()).onAttached(stream);
        verify(mIntSubscriber, never()).onDetached(stream);

        stream.subscribe(mIntSubscriber);
        verify(mIntSubscriber).onAttached(stream);
        verify(mIntSubscriber).onStateChanged(stream, State.OPEN);

        stream.close();
        verify(mIntSubscriber).onStateChanged(stream, State.CLOSED);

        stream.remove(mIntSubscriber);
        verify(mIntSubscriber).onDetached(stream);

        verifyNoMoreInteractions(mIntSubscriber);
    }

    @Test
    public void testStreamIdle() throws Exception {
        IStream<Integer> stream = Streams.<Integer>create().on(Schedulers.sync());

        assertThat(stream.state()).isEqualTo(State.IDLE);

        stream.subscribe(mIntSubscriber);
        assertThat(stream.state()).isEqualTo(State.OPEN);
        verify(mIntSubscriber).onStateChanged(stream, State.OPEN);

        stream.remove(mIntSubscriber);
        assertThat(stream.state()).isEqualTo(State.IDLE);
        verify(mIntSubscriber, never()).onStateChanged(stream, State.IDLE);

        stream.publish(42);
        verify(mIntSubscriber, never()).onReceive(stream, 42);

        stream.close();
        assertThat(stream.state()).isEqualTo(State.CLOSED);
        verify(mIntSubscriber, never()).onStateChanged(stream, State.CLOSED);
    }

    @Test
    public void testStreamAsync() throws Exception {

        CountDownLatch resultLatch = new CountDownLatch(1);
        CountDownLatch closetLatch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger(0);

        IStream<Integer> stream = Streams.<Integer>create().on(Schedulers.io());

        Subscribers.Adapter<Integer> subscriber = new Subscribers.Adapter<Integer>() {
            @Override
            public void onReceive(@NonNull IStream<? extends Integer> stream, Integer integer) {
                result.set(integer);
                resultLatch.countDown();
            }

            @Override
            public void onStateChanged(@NonNull IStream<? super Integer> stream, @NonNull State newState) {
                if (newState == State.CLOSED) {
                    closetLatch .countDown();
                }
            }
        };

        stream.subscribe(subscriber, Schedulers.io());
        stream.publish(42);

        // Note: calling stream.close() here might close the stream BEFORE the async publish has a chance to
        // run, in which case it would get cancel since a closed stream does not publish events.
        // Stream state changes are instant and do not run on the stream's scheduler thread.

        resultLatch.await();
        assertThat(result.get()).isEqualTo(42);

        stream.close();
        closetLatch.await();
    }
}
