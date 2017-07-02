package com.alflabs.utils.v1;

import android.util.Log;

public class AndroidLog {
    public static void d(String tag, String message, Throwable tr) {
        Log.d(tag, message, tr);
    }

    public static void d(String tag, String message) {
        Log.d(tag, message);
    }
}
