package com.alflabs.app;

import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;

public class BusesTest {

    @SuppressWarnings("PointlessArithmeticExpression")
    @Test
    public void testNewBus() throws Exception {
        Bus b0 = Buses.newBus();
        assertThat(b0).isNotNull();
        assertThat(b0.getId()).isGreaterThan(0);
        assertThat(b0).isSameAs(Buses.getById(b0.getId()));

        Bus b1 = Buses.newBus();
        assertThat(b1).isNotNull();
        assertThat(b1.getId()).isEqualTo(b0.getId() + 1);
        assertThat(b1).isSameAs(Buses.getById(b1.getId()));
    }
}
