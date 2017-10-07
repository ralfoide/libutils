package com.alflabs.utils;

@SuppressWarnings("WeakerAccess")
public class RPair<A, B> {
    public final A first;
    public final B second;

    protected RPair(A a, B b) {
        first = a;
        second = b;
    }

    public static <A, B> RPair<A, B> create(A a, B b) {
        return new RPair<>(a, b);
    }

    @Override
    public String toString() {
        return "{" + first + ", " + second + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }

        RPair<?, ?> rhs = (RPair<?, ?>) o;

        return (first != null ? first.equals(rhs.first) : rhs.first == null)
                && (second != null ? second.equals(rhs.second) : rhs.second == null);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}
