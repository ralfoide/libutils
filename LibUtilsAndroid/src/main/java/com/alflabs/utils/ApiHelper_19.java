/*
 * Project: AndroidAppLib
 * Copyright (C) 2015 ralfoide gmail com.
 */

package com.alflabs.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;

import com.alflabs.annotations.NonNull;

/**
 * API 19: support ActivityManager_isLowRamDevice
 */
@TargetApi(19)
class ApiHelper_19 extends ApiHelper_18 {

    /**
     * Calls ActivityManager.isLowRamDevice() on API 19+.
     * Returns true below API 19.
     */
    public boolean ActivityManager_isLowRamDevice(@NonNull ActivityManager activityManager) {
        return activityManager.isLowRamDevice();
    }
}
