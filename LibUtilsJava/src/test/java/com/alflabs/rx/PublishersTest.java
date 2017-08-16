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

import static com.google.common.truth.Truth.assertThat;

public class PublishersTest {
    @Rule public MockitoRule mRule = MockitoJUnit.rule();

    @Test
    public void testStreamPublishSync() throws Exception {
        ArrayList<Integer> result = new ArrayList<>();

        Streams.<Integer>create()
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
    public void testStreamPublishGeneratorAsync1() throws Exception {
        List<Integer> result = Collections.synchronizedList(new ArrayList<Integer>());
        CountDownLatch latch = new CountDownLatch(1);

        Streams.<Integer>create()
                .on(Schedulers.io())
                .publishWith(new Publishers.Adapter<Integer>() {
                    @Override
                    public void onAttached(@NonNull IStream<? super Integer> stream) {
                        for (int i = 0; !stream.isClosed(); i++) {
                            stream.publish(42 + i);
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

        latch.await();
        assertThat(result.toArray()).isEqualTo(new Object[] { 42, 43, 44, 45, 46, 47 });
    }

    /**
     * Example of a generator that sends values on a timer forever till the last subscriber
     * is detached.
     */
    @Test
    public void testStreamPublishGeneratorAsync2() throws Exception {
        List<Integer> result = Collections.synchronizedList(new ArrayList<Integer>());
        CountDownLatch latch = new CountDownLatch(1);

        IStream<Integer> stream = Streams.<Integer>create()
                .on(Schedulers.io())
                .publishWith(new Publishers.Adapter<Integer>() {
                    @Override
                    public void onAttached(@NonNull IStream<? super Integer> stream) {
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
                            stream.publish(42 + i);
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

        latch.await();
        assertThat(result.toArray()).isEqualTo(new Object[] { 42, 43, 44, 45, 46 });
    }
}
