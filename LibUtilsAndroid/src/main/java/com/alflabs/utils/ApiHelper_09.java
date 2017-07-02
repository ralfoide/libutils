/*
 * Project: AndroidAppLib
 * Copyright (C) 2015 ralfoide gmail com.
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
