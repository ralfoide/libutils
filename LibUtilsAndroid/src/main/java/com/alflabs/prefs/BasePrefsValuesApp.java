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

public class BasePrefsValuesApp extends BasePrefsValues {

    private static final String PREF_DISMISS_INTRO = "dismiss_intro";
    private static final String PREF_LAST_INTRO_VERS = "last_intro_vers";
    private static final String PREF_LAST_EXCEPTIONS = "last_exceptions";
    private static final String PREF_LAST_ACTIONS = "last_actions";

    public BasePrefsValuesApp(Context context) {
        super(context);
    }

    public BasePrefsValuesApp(SharedPreferences prefs) {
        super(prefs);
    }

    public boolean isIntroDismissed() {
        return mPrefs.getBoolean(PREF_DISMISS_INTRO, false);
    }

    public void setIntroDismissed(boolean dismiss) {
        synchronized (editLock()) {
            endEdit(startEdit().putBoolean(PREF_DISMISS_INTRO, dismiss));
        }
    }

    public int getLastIntroVersion() {
        return mPrefs.getInt(PREF_LAST_INTRO_VERS, 0);
    }

    public void setLastIntroVersion(int lastIntroVers) {
        synchronized (editLock()) {
            endEdit(startEdit().putInt(PREF_LAST_INTRO_VERS, lastIntroVers));
        }
    }

    public String getLastExceptions() {
        return mPrefs.getString(PREF_LAST_EXCEPTIONS, null);
    }

    public void setLastExceptions(String s) {
        synchronized (editLock()) {
            endEdit(startEdit().putString(PREF_LAST_EXCEPTIONS, s));
        }
    }

    public String getLastActions() {
        return mPrefs.getString(PREF_LAST_ACTIONS, null);
    }

    public void setLastActions(String s) {
        synchronized (editLock()) {
            endEdit(startEdit().putString(PREF_LAST_ACTIONS, s));
        }
    }

}
