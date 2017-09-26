package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class StreamsTest {
    @Rule public MockitoRule mRule = MockitoJUnit.rule();

    @Mock _SubAdapter<Integer> mIntSubscriber;

    @Test
    public void testStreamPublish1() throws Exception {
        AtomicInteger result = new AtomicInteger(0);

        IPublisher<Integer> publisher = Publishers.publisher();
        IStream<Integer> stream = Streams.<Integer>stream()
                .on(Schedulers.sync())
                .subscribe((strm, integer) -> result.set(integer))
                .publishWith(publisher);
        publisher.publish(42);
        stream.close();

        assertThat(result.get()).isEqualTo(42);
    }

    @Test
    public void testStreamNulls() throws Exception {
        // Null objects are perfectly valid stream event values.
        AtomicReference<Object> result = new AtomicReference<>(new Object());
        assertThat(result.get()).isNotNull();

        IPublisher<Object> publisher = Publishers.publisher();
        IStream<Object> stream = Streams.<Object>stream()
                .on(Schedulers.sync())
                .subscribe((strm, event) -> result.set(event))
                .publishWith(publisher);
        publisher.publish(null);
        stream.close();

        assertThat(result.get()).isNull();
    }

    @Test
    public void testStreamPublish2() throws Exception {
        AtomicInteger result = new AtomicInteger(0);
        ISubscriber<Integer> subscriber = (stream, integer) -> result.set(integer);

        IPublisher<Integer> publisher = Publishers.publisher();
        IStream<Integer> stream = Streams.<Integer>stream().on(Schedulers.sync()).publishWith(publisher);
        assertThat(stream.getState()).isEqualTo(State.IDLE);
        assertThat(stream.isIdle()).isTrue();

        stream.subscribe(subscriber, Schedulers.sync());
        assertThat(stream.getState()).isEqualTo(State.OPEN);
        assertThat(stream.isOpen()).isTrue();
        assertThat(stream.isIdle()).isFalse();
        assertThat(stream.isClosed()).isFalse();

        publisher.publish(42);
        stream.close();
        assertThat(result.get()).isEqualTo(42);
        assertThat(stream.isClosed()).isTrue();
    }

    @Test
    public void testStreamPublish3() throws Exception {
        IPublisher<Integer> publisher = Publishers.publisher();
        IStream<Integer> stream = Streams.<Integer>stream()
                .on(Schedulers.sync())
                .subscribe(mIntSubscriber)
                .publishWith(publisher);
        publisher.publish(42)
                .publish(43);
        stream.close();

        verify(mIntSubscriber).onAttached(stream);
        verify(mIntSubscriber).onStateChanged(stream, State.OPEN);
        verify(mIntSubscriber).onReceive(stream, 42);
        verify(mIntSubscriber).onReceive(stream, 43);
        verify(mIntSubscriber).onStateChanged(stream, State.CLOSED);
        verifyNoMoreInteractions(mIntSubscriber);
    }

    @Test
    public void testStreamPause() throws Exception {
        IPublisher<Integer> publisher = Publishers.publisher();
        IStream<Integer> stream = Streams.<Integer>stream()
                .on(Schedulers.sync())
                .subscribe(mIntSubscriber)
                .publishWith(publisher);
        publisher.publish(42)
                .publish(43);
        stream.pause();
        publisher.publish(44)
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

        IStream<Integer> stream = Streams.<Integer>stream().on(Schedulers.sync());
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
        IPublisher<Integer> publisher = Publishers.publisher();
        IStream<Integer> stream = Streams.<Integer>stream().on(Schedulers.sync()).publishWith(publisher);

        assertThat(stream.getState()).isEqualTo(State.IDLE);
        assertThat(stream.isIdle()).isTrue();

        stream.subscribe(mIntSubscriber);
        assertThat(stream.getState()).isEqualTo(State.OPEN);
        assertThat(stream.isOpen()).isTrue();
        assertThat(stream.isIdle()).isFalse();
        assertThat(stream.isClosed()).isFalse();
        verify(mIntSubscriber).onStateChanged(stream, State.OPEN);

        stream.remove(mIntSubscriber);
        assertThat(stream.getState()).isEqualTo(State.IDLE);
        assertThat(stream.isIdle()).isTrue();
        verify(mIntSubscriber, never()).onStateChanged(stream, State.IDLE);

        publisher.publish(42);
        verify(mIntSubscriber, never()).onReceive(stream, 42);

        stream.close();
        assertThat(stream.getState()).isEqualTo(State.CLOSED);
        assertThat(stream.isClosed()).isTrue();
        verify(mIntSubscriber, never()).onStateChanged(stream, State.CLOSED);
    }

    @Test
    public void testStreamAsync() throws InterruptedException {

        CountDownLatch resultLatch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger(0);

        IPublisher<Integer> publisher = Publishers.publisher();
        IStream<Integer> stream = Streams.<Integer>stream().on(Schedulers.io()).publishWith(publisher);

        _SubAdapter<Integer> subscriber = new _SubAdapter<Integer>() {
            @Override
            public void onReceive(@NonNull IStream<? extends Integer> stream, Integer integer) {
                result.set(integer);
                resultLatch.countDown();
            }

            @Override
            public void onStateChanged(@NonNull IStream<? super Integer> stream, @NonNull State newState) {
                if (newState == State.CLOSED) {
                    closeLatch .countDown();
                }
            }
        };

        stream.subscribe(subscriber, Schedulers.io());
        publisher.publish(42);

        // Note: calling stream.close() here might close the stream BEFORE the async publish has a chance to
        // run, in which case it would get cancel since a closed stream does not publish events.
        // Stream state changes are instant and do not run on the publisher's scheduler thread.

        resultLatch.await(5, TimeUnit.SECONDS);
        assertThat(result.get()).isEqualTo(42);

        stream.close();
        closeLatch.await(5, TimeUnit.SECONDS);
    }

    private abstract class BasePublisherWithSubscriberAttached<E>
            extends BasePublisher<E> implements ISubscriberAttached<E> {}

    @Test
    public void testSubscriberAttached() throws Exception {
        AtomicInteger result = new AtomicInteger(0);
        AtomicBoolean subscriberAttached = new AtomicBoolean();

        IStream<Integer> stream = Streams
                .<Integer>stream()
                .on(Schedulers.sync());

        IPublisher<Integer> publisher = new BasePublisherWithSubscriberAttached<Integer>() {
            @Override
            public void onSubscriberAttached(@NonNull IStream<? super Integer> s, @NonNull ISubscriber<? super Integer> subscriber) {
                assertThat(s).isSameAs(stream);
                subscriberAttached.set(true);
            }

            @Override
            public void onSubscriberDetached(@NonNull IStream<? super Integer> s, @NonNull ISubscriber<? super Integer> subscriber) {
                assertThat(s).isSameAs(stream);
                subscriberAttached.set(false);
            }
        };

        stream.publishWith(publisher);

        assertThat(subscriberAttached.get()).isFalse();

        ISubscriber<Integer> subscriber = (streas, integer) -> result.set(integer);
        stream.subscribe(subscriber);
        assertThat(subscriberAttached.get()).isTrue();

        publisher.publish(42);
        assertThat(result.get()).isEqualTo(42);

        stream.remove(subscriber);
        assertThat(subscriberAttached.get()).isFalse();
    }
}
