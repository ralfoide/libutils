/*
 * Project: AndroidAppLib
 * Copyright (C) 2014 ralfoide gmail com.
 */

package com.alflabs.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.view.View;

import com.alflabs.annotations.NonNull;

/**
 * API 17: support Context_getScreenWidth
 */
@TargetApi(17)
class ApiHelper_17 extends ApiHelper_16 {

    /**
     * Generate a value suitable for use in View.setId(int).
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public int View_generateViewId() {
        return View.generateViewId();
    }

    /**
     * Returns Activity.isDestroyed() for API 17+.
     * Always returns false for API < 17.
     */
    public boolean Activity_isDestroyed(@NonNull Activity activity) {
        return activity.isDestroyed();
    }
}
