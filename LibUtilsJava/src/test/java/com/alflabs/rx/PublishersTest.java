package com.alflabs.rx;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import static com.google.common.truth.Truth.assertThat;

public class PublishersTest {
    @Rule public MockitoRule mRule = MockitoJUnit.rule();

    @Test
    public void testStreamPublish1() throws Exception {
        ArrayList<Integer> result = new ArrayList<>();

        Streams.<Integer>create()
                .on(Schedulers.sync())
                .publishWith(Publishers.just(42, 43, 44, 45))
                .subscribe((stream, integer) -> result.add(integer))
                .close();
        assertThat(result.toArray()).isEqualTo(new Object[] { 42, 43, 44, 45 });
    }
}
