/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;

/**
 * API 13: support Context_getScreenWidth
 */
@TargetApi(13)
class ApiHelper_13 extends ApiHelper_11 {

    /**
     * Returns {@link Configuration#screenWidthDp}, defined only starting with API 13.
     */
    @Override
    public int Context_getScreenWidth(Context appContext) {
        Configuration config = appContext.getResources().getConfiguration();
        return config.screenWidthDp;
    }

}
