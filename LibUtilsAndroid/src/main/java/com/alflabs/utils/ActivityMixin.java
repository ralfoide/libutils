/*
 * Project: Lib Utils
 * Copyright (C) 2017 alf.labs gmail com,
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

import android.app.Activity;
import android.util.Log;
import com.alflabs.libutils.BuildConfig;

public class ActivityMixin<A extends Activity> {
    private static final String TAG = ActivityMixin.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    protected final A mActivity;

    public ActivityMixin(A activity) {
        mActivity = activity;
    }

    public A getActivity() {
        return mActivity;
    }

    public void onCreate() {
        if (DEBUG) Log.d(TAG, "onCreate");
    }

    public void onStart() {
        if (DEBUG) Log.d(TAG, "onStart");
    }

    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume");
    }

    public void onPause() {
        if (DEBUG) Log.d(TAG, "onPause");
    }

    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop");
    }

    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
    }
}
