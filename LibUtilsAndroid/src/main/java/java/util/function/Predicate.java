package java.util.function;

import android.annotation.SuppressLint;

/**
 * DUMMY VERSION for Java 1.8 compile-time compatibility for InjectionValidator.java
 * <p/>
 * FIXME: Remove once can use proper Java 1.8 without Jack compiler in Android Library (with AS 3.0+)
 * <p/>
 * Represents a predicate (boolean-valued function) of one argument.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #test(Object)}.
 */
@FunctionalInterface
@SuppressLint("NewApi")
public interface Predicate<T> {
    /**
     * Tests this operation on the given argument.
     */
    boolean test(T value);
}
