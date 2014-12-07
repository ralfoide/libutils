/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

/**
 * API 17: support Context_getScreenWidth
 */
@TargetApi(17)
class ApiHelper_17 extends ApiHelper_13 {

    /**
     * Generate a value suitable for use in View.setId(int).
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public int View_generateViewId() {
        return View.generateViewId();
    }
}
