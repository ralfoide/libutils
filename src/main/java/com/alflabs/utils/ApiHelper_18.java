/*
 * Project: AndroidAppLib
 * Copyright (C) 2015 ralfoide gmail com.
 */

package com.alflabs.utils;

import android.annotation.TargetApi;
import android.os.Looper;

import com.alflabs.annotations.NonNull;

/**
 * API 18: support Looper_quitSafely
 */
@TargetApi(18)
class ApiHelper_18 extends ApiHelper_17 {

    /**
     * Calls Looper.quitSafely() on API 18+.
     * Calls the regular Looper.quit() below API 18.
     */
    public void Looper_quitSafely(@NonNull Looper looper) {
        looper.quitSafely();
    }
}
