/*
 * Project: Lib Utils
 * Copyright (C) 2012 alf.labs gmail com,
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

import com.alflabs.app.BackupWrapper;
import com.alflabs.libutils.BuildConfig;
import com.alflabs.serial.AsyncSerialFile;
import com.alflabs.serial.SerialReader;
import com.alflabs.serial.SerialWriter;

import android.content.Context;

/**
 * Wrapper around {@link SerialWriter} and {@link SerialReader} to deal with app prefs.
 * <p/>
 * Supported types are the minimal required for our needs: boolean, string and int.
 * Callers need to ensure that only one instance exists for the same file.
 * <p/>
 * Caller initial cycle should be:
 * - begingReadAsync
 * - endReadAsync ... this waits for the read the finish.
 * - read, add or modify data.
 * - modifying data generates a delayed write (or delays an existing one)
 * - flushSync must be called by the owner at least once, typically when an activity/app
 *   is paused or about to finish. It forces a write or wait for an existing one to finish.
 * <p/>
 * Values cannot be null.
 * Affecting a value to null is equivalent to removing it from the storage map.
 */
public class BasePrefsStorage extends AsyncSerialFile {

    @SuppressWarnings("unused")
    private static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String TAG = BasePrefsStorage.class.getSimpleName();

    /*
     * All prefs key constants used.
     * IMPORTANT: once set, value must NOT be changed.
     */
    /** User-specific issue ID. Used to identify this user in feedback reports. Type: String. */
    public final static String ISSUE_ID = "issue_id";

    /**
     * File header. Formatted to be 8 bytes (hoping it will help alignment).
     * The C identifies this is for 24 *C*lock.
     * The 1 can serve has a format version number in case we want to have future versions.
     */
    private static final byte[] HEADER = new byte[] {
        'P', 'R', 'E', 'F',
        '-', 'C', '1', '\0'};

    /**
     * Opens a serial prefs for "filename.sprefs" in the app's dir.
     * Caller must still read the file before anything happens.
     *
     * @param filename The end leaf filename. Must not be null or empty.
     *   Must not contain any path separator.
     *   This is not an absolute path -- the actual path will depend on the application package.
     */
    public BasePrefsStorage(String filename) {
        super(filename);
    }

    @Override
    public byte[] getHeader() {
        return HEADER;
    }

    /**
     * Called after successfully writing a file asynchronously.
     *
     * @param appContext The {@link Context} to use.
     */
    protected void postWriteAsync(Context appContext) {
        try {
            // Notify the backup manager that data might have changed
            new BackupWrapper(appContext).dataChanged();
        } catch (Exception ignore) {
        }
    }
}
