package com.alflabs.utils;

import android.util.Log;

public class AndroidLogger implements ILogger {
    public void d(String tag, String message, Throwable tr) {
        Log.d(tag, message, tr);
    }

    public void d(String tag, String message) {
        Log.d(tag, message);
    }
}
