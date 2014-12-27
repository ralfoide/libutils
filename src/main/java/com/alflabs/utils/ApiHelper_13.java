/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.Display;

/**
 * API 13: support Context_getScreenWidth, Display_getScreenSize
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

    /**
     * Returns the {@link Display#getSize} with is defined starting with API 13.
     * For API 10, uses {@link Display#getWidth} and {@link Display#getHeight}
     */
    public Point Display_getSize(Display display) {
        Point size = new Point(0, 0);
        display.getSize(size);
        return size;
    }

}
