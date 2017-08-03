package com.alflabs.sub;

public interface ISubscriber<T> {
    void onEmitted(T object);
}
