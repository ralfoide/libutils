/*
 * Project: Timeriffic
 * Copyright (C) 2011 ralfoide gmail com,
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

package com.alfray.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public interface IActivityDelegate {
    /**
     * @see Activity#onCreate
     */
    public void onCreate(Bundle savedInstanceState);

    /**
     * @see Activity#onResume
     */
    public void onResume();

    /**
     * @see Activity#onPause
     */
    public void onPause();

    /**
     * @see Activity#onStop
     */
    public void onStop();

    /**
     * @see Activity#onRestoreInstanceState
     */
    public void onRestoreInstanceState(Bundle savedInstanceState);

    /**
     * @see Activity#onSaveInstanceState
     */
    public void onSaveInstanceState(Bundle outState);

    /**
     * @see Activity#onDestroy
     */
    public void onDestroy();

    /**
     * @see Activity#onActivityResult
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * @see Activity#onCreateDialog
     */
    public Dialog onCreateDialog(int id);

    /**
     * @see Activity#onContextItemSelected
     */
    public boolean onContextItemSelected(MenuItem item);

    /**
     * @see Activity#onCreateOptionsMenu
     */
    public void onCreateOptionsMenu(Menu menu);

    /**
     * @see Activity#onOptionsItemSelected
     */
    public boolean onOptionsItemSelected(MenuItem item);
}
