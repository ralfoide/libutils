/*
 * Project: Lib Utils
 * Copyright (C) 2015 alf.labs gmail com,
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
