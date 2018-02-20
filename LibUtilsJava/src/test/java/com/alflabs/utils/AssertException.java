package com.alflabs.utils;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

public class AssertException {

    public interface RunnableException {
        void run() throws Throwable;
    }

    public static <E extends Throwable> void assertException(Class<E> clazz, RunnableException test) {
        try {
            test.run();
            fail("Expected Exception: " + clazz.toString());
        } catch (Throwable t) {
            assertWithMessage("\nExpected Exception: %s.\nActual Exception  : %s.\nDetails", clazz, t.getClass())
                    .that(t.getClass()).isEqualTo(clazz);
        }
    }
}
