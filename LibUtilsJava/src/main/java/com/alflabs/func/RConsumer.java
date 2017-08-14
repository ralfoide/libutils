package com.alflabs.func;

/**
 * Mirror of the JDK 8 functional interface Consumer to use on Android < 24 that lacks such implementations.
 * <p/>
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object)}.
 */
@FunctionalInterface
public interface RConsumer<T> {
    /**
     * Performs this operation on the given argument.
     */
    void accept(T value);
}
