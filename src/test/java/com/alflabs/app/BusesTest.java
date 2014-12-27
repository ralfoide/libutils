package com.alflabs.app;

import org.junit.Test;

import static org.junit.Assert.*;

public class BusesTest {

    @SuppressWarnings("PointlessArithmeticExpression")
    @Test
    public void testNewBus() throws Exception {
        Bus b0 = Buses.newBus();
        assertNotNull(b0);
        assertTrue(b0.getId() > 0);
        assertSame(b0, Buses.getById(b0.getId()));

        Bus b1 = Buses.newBus();
        assertNotNull(b1);
        assertEquals(b0.getId() + 1, b1.getId());
        assertSame(b1, Buses.getById(b1.getId()));
    }
}
