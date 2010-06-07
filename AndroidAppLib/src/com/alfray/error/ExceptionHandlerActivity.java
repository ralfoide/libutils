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

package com.alfray.error;


import android.app.Activity;
import android.os.Bundle;

/**
 * An {@link Activity} base class that uses our {@link ExceptionHandler}.
 */
public abstract class ExceptionHandlerActivity extends Activity {

    private ExceptionHandler mExceptionHandler;

    @Override
    protected void onCreate(Bundle bundle) {
        mExceptionHandler = new ExceptionHandler(this);
        super.onCreate(bundle);
    }

    @Override
    protected void onStart() {
        if (mExceptionHandler == null) {
            mExceptionHandler = new ExceptionHandler(this);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mExceptionHandler != null) {
            mExceptionHandler.detach();
            mExceptionHandler = null;
        }
    }

}
