package java.util.function;

/**
 * DUMMY VERSION for Java 1.8 Compile-time Compatibility for InjectionValidator.java.
 * <p/>
 * FIXME: Remove once can use proper Java 1.8 without Jack compiler in Android Library (with AS 3.0+)
 * <p/>
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object)}.
 */
@FunctionalInterface
public interface Consumer<T> {
    /**
     * Performs this operation on the given argument.
     */
    void accept(T value);
}
