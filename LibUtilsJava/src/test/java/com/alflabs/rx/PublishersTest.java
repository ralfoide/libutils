/*
 * Project: Lib Utils
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    @Test
    public void testPublishSingleAttach() throws Exception {
        IPublisher<Integer> publisher = Publishers.latest();

        IStream<Integer> stream1 = Streams.<Integer>stream().on(Schedulers.sync());
        IStream<Integer> stream2 = Streams.<Integer>stream().on(Schedulers.sync());

        stream1.publishWith(publisher);

        // Trying to publish with the same subscriber on a second stream throws an exception
        Exception expected = null;
        try {
            stream2.publishWith(publisher);
        } catch (PublisherAttachedException exception) {
            expected = exception;
        }
        assertThat(expected).isInstanceOf(PublisherAttachedException.class);

        // However the publisher can first be detached then reattached properly.
        stream1.remove(publisher);
        stream2.publishWith(publisher);
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
                .publishWith(new BaseGenerator<Integer>() {
                    @Override
                    public void onAttached(@NonNull IStream<? super Integer> stream) {
                        // Calling super.onAttached is important as the base publisher needs it to publish something.
                        super.onAttached(stream);

                        for (int i = 0; !stream.isClosed(); i++) {
                            publishOnStream(42 + i);
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
                .publishWith(new BaseGenerator<Integer>() {
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
                            publishOnStream(42 + i);
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
