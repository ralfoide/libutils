package com.alflabs.sub;

import android.util.Log;
import com.alflabs.libutils.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class Emitter<T> {
    private String TAG = this.getClass().getSimpleName();
    private final boolean DEBUG = BuildConfig.DEBUG;

    private final Object mLock = new Object();
    /** List must be synchronized by lock. */
    private final List<ISubscriber<T>> mSubscribers = new ArrayList<>();
    private T mLatest = null;

    public Emitter() {}

    public void subscribe(ISubscriber<T> subscriber) {
        synchronized (mLock) {
            if (!mSubscribers.contains(subscriber)) {
                mSubscribers.add(subscriber);
                if (mLatest != null) {
                    doEmit(subscriber, mLatest);
                }
            }
        }
    }

    public void unsubscribe(ISubscriber<T> subscriber) {
        synchronized (mLock) {
            mSubscribers.remove(subscriber);
        }
    }

    public void emit(T object) {
        mLatest = object;
        synchronized (mLock) {
            for (ISubscriber<T> subscriber : mSubscribers) {
                doEmit(subscriber, object);
            }
        }
    }

    private void doEmit(ISubscriber<T> subscriber, T object) {
        try {
            subscriber.onEmitted(object);
        } catch (Exception e) {
            if (DEBUG) Log.d(TAG, "Subscriber.onEmitted failed", e);
        }
    }
}
