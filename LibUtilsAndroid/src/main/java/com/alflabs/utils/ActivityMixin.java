package com.alflabs.utils;

import android.app.Activity;
import android.util.Log;
import com.alflabs.libutils.BuildConfig;

public class ActivityMixin<A extends Activity> {
    private static final String TAG = ActivityMixin.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    protected final A mActivity;

    public ActivityMixin(A activity) {
        mActivity = activity;
    }

    public A getActivity() {
        return mActivity;
    }

    public void onCreate() {
        if (DEBUG) Log.d(TAG, "onCreate");
    }

    public void onStart() {
        if (DEBUG) Log.d(TAG, "onStart");
    }

    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume");
    }

    public void onPause() {
        if (DEBUG) Log.d(TAG, "onPause");
    }

    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop");
    }

    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
    }
}
