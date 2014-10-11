/*
 * Project: AndroidAppLib
 * Copyright (C) 2010 ralfoide gmail com,
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
import android.util.Log;

public class BasePrefsValues {

    protected final SharedPreferences mPrefs;

    public BasePrefsValues(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public BasePrefsValues(SharedPreferences prefs) {
        mPrefs = prefs;
    }

    public SharedPreferences getPrefs() {
        return mPrefs;
    }

    public Object editLock() {
        return BasePrefsValues.class;
    }

    /** Returns a shared pref editor. Must call endEdit() later. */
    public Editor startEdit() {
        return mPrefs.edit();
    }

    /** Commits an open editor. */
    public boolean endEdit(Editor e, String tag) {
        boolean b = e.commit();
        if (!b) Log.w(tag, "Prefs.edit.commit failed");
        return b;
    }

    public boolean isIntroDismissed() {
        return mPrefs.getBoolean("dismiss_intro", false);
    }

    public void setIntroDismissed(boolean dismiss) {
        synchronized (editLock()) {
            mPrefs.edit().putBoolean("dismiss_intro", dismiss).commit();
        }
    }

    public int getLastIntroVersion() {
        return mPrefs.getInt("last_intro_vers", 0);
    }

    public void setLastIntroVersion(int lastIntroVers) {
        synchronized (editLock()) {
            mPrefs.edit().putInt("last_intro_vers", lastIntroVers).commit();
        }
    }

    public String getLastExceptions() {
        return mPrefs.getString("last_exceptions", null);
    }

    public void setLastExceptions(String s) {
        synchronized (editLock()) {
            mPrefs.edit().putString("last_exceptions", s).commit();
        }
    }

    public String getLastActions() {
        return mPrefs.getString("last_actions", null);
    }

    public void setLastActions(String s) {
        synchronized (editLock()) {
            mPrefs.edit().putString("last_actions", s).commit();
        }
    }

}
