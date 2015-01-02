/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.prefs;

import com.alflabs.annotations.PublicForTesting;
import com.alflabs.app.BackupWrapper;
import com.alflabs.serial.AsyncSerialFile;
import com.alflabs.serial.SerialKey;
import com.alflabs.serial.SerialReader;
import com.alflabs.serial.SerialWriter;

import android.content.Context;
import android.util.Log;
import com.alflabs.utils.RSparseArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;

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

    private static final boolean DEBUG = true;
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
