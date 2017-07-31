package com.alflabs.utils;

import android.app.Service;
import android.util.Log;
import com.alflabs.libutils.BuildConfig;

public class ServiceMixin<A extends Service> {
    private final String TAG = this.getClass().getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    protected A mService;

    public ServiceMixin() {
    }

    public A getService() {
        return mService;
    }

    public void onCreate(A service) {
        mService = service;
        if (DEBUG) Log.d(TAG, "onCreate");
    }

    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
    }
}
