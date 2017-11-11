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
import android.content.SharedPreferences;

import com.alflabs.annotations.NonNull;

/**
 * API 0: support SharedPreferences.Editor.apply
 */
@TargetApi(9)
class ApiHelper_09 extends ApiHelper {

    /**
     * Calls SharedPreferences.Editor.apply() on API 9+.
     * Calls SharedPreferences.Editor.commit() on API < 9 but does not return any result.
     */
    public void SharedPreferences_Editor_apply(@NonNull SharedPreferences.Editor editor) {
        editor.apply();
    }
}
