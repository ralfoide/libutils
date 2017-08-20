package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;

public class PublishersTest {
    @Rule public MockitoRule mRule = MockitoJUnit.rule();

    @Test
    public void testPublishSync() throws Exception {
        ArrayList<Integer> result = new ArrayList<>();

        Streams.<Integer>stream()
                .on(Schedulers.sync())
                .publishWith(Publishers.just(42, 43, 44, 45))
                .subscribe((stream, integer) -> result.add(integer))
                .close();
        assertThat(result.toArray()).isEqualTo(new Object[] { 42, 43, 44, 45 });
    }

    /**
     * Example of a generator that sends values on a timer forever till the stream is closed.
     */
    @Test
    public void testPublishGeneratorAsync1() throws Exception {
        List<Integer> result = Collections.synchronizedList(new ArrayList<Integer>());
        CountDownLatch latch = new CountDownLatch(1);

        Streams.<Integer>stream()
                .on(Schedulers.io())
                .publishWith(new BasePublisher<Integer>() {
                    @Override
                    public void onAttached(@NonNull IStream<? super Integer> stream) {
                        // Calling super.onAttached is important as the base publisher needs it to publish something.
                        super.onAttached(stream);

                        for (int i = 0; !stream.isClosed(); i++) {
                            publish(42 + i);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ignore) {
                                break;
                            }
                        }
                        // end the generator and test.
                        latch.countDown();
                    }
                })
                .subscribe(new ISubscriber<Integer>() {
                    @Override
                    public void onReceive(@NonNull IStream<? extends Integer> stream, @Null Integer integer) {
                        if (integer == null) {
                            return;
                        }
                        result.add(integer);
                        if (integer == 42 + 5) {
                            stream.close();
                        }
                    }
                });

        latch.await(5, TimeUnit.SECONDS);
        assertThat(result.toArray()).isEqualTo(new Object[] { 42, 43, 44, 45, 46, 47 });
    }

    /**
     * Example of a generator that sends values on a timer forever till the last subscriber
     * is detached.
     */
    @Test
    public void testPublishGeneratorAsync2() throws Exception {
        List<Integer> result = Collections.synchronizedList(new ArrayList<Integer>());
        CountDownLatch latch = new CountDownLatch(1);

        IStream<Integer> stream = Streams.<Integer>stream()
                .on(Schedulers.io())
                .publishWith(new BasePublisher<Integer>() {
                    @Override
                    public void onAttached(@NonNull IStream<? super Integer> stream) {
                        // Calling super.onAttached is important as the base publisher needs it to publish something.
                        super.onAttached(stream);

                        // Wait for the stream to go from idle to open
                        while (stream.isIdle()) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ignore) {
                                break;
                            }
                        }

                        // Once open, generate values till it becomes closed or idle again.
                        for (int i = 0; !stream.isClosed() && !stream.isIdle(); i++) {
                            publish(42 + i);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ignore) {
                                break;
                            }
                        }

                        // end the generator and test.
                        latch.countDown();
                    }
                });

        assertThat(stream.isIdle()).isTrue();

        // Once the subscriber is added, the generator will start.
        stream.subscribe(new ISubscriber<Integer>() {
                    @Override
                    public void onReceive(@NonNull IStream<? extends Integer> stream, @Null Integer integer) {
                        if (integer == null) {
                            return;
                        }
                        result.add(integer);
                        if (integer == 42 + 4) {
                            stream.remove(this);
                        }
                    }
                });

        assertThat(stream.isIdle()).isFalse();

        latch.await(5, TimeUnit.SECONDS);
        assertThat(result.toArray()).isEqualTo(new Object[] { 42, 43, 44, 45, 46 });
    }

    @Test
    public void testPublishLatest() throws Exception {
        ArrayList<Integer> result = new ArrayList<>();

        IPublisher<Integer> publisher = Publishers.latest();
        IStream<Integer> stream = Streams.<Integer>stream()
                .on(Schedulers.sync())
                .publishWith(publisher);
        publisher.publish(42)
                .publish(43);

        stream.subscribe((s, integer) -> result.add(integer));
        publisher.publish(44);

        stream.close();
        assertThat(result.toArray()).isEqualTo(new Object[] { 43, 44 });    }
}
