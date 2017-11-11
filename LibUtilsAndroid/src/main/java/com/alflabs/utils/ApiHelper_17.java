/*
 * Project: Lib Utils
 * Copyright (C) 2014 alf.labs gmail com,
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
