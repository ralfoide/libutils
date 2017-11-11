/*
 * Project: Lib Utils
 * Copyright (C) 2012 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
