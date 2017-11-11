/*
 * Project: Lib Utils
 * Copyright (C) 2010 alf.labs gmail com,
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


package com.alflabs.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.alflabs.annotations.NonNull;
import com.alflabs.utils.ApiHelper;

public class BasePrefsValues {

    protected final SharedPreferences mPrefs;

    public BasePrefsValues(@NonNull Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public BasePrefsValues(@NonNull SharedPreferences prefs) {
        mPrefs = prefs;
    }

    @NonNull
    public SharedPreferences getPrefs() {
        return mPrefs;
    }

    @NonNull
    public Object editLock() {
        return BasePrefsValues.class;
    }

    /** Returns a shared pref editor. Must call endEdit() later. */
    @NonNull
    public Editor startEdit() {
        return mPrefs.edit();
    }

    /** Commits an open editor. */
    public void endEdit(@NonNull Editor editor) {
        ApiHelper.get().SharedPreferences_Editor_apply(editor);
    }
}
