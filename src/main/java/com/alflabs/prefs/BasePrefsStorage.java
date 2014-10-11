/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.prefs;

import com.alflabs.annotations.PublicForTesting;
import com.alflabs.app.BackupWrapper;
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
public class BasePrefsStorage {

    /*
     * All prefs key constants used.
     * IMPORTANT: once set, value must NOT be changed.
     */
    /** User-specific issue ID. Used to identify this user in feedback reports. Type: String. */
    public final static String ISSUE_ID = "issue_id";

    private static final boolean DEBUG = true;
    public static final String TAG = BasePrefsStorage.class.getSimpleName();

    /**
     * Special flag to rethrow exceptions when loading/saving fails.
     * In normal usage, callers will not see exceptions but load/save will just
     * return false in case of error, to not interrupt normal workflow. However
     * during unit tests we want hard errors.
     */
    public static boolean THROW_EXCEPTIONS_WHEN_TESTING = false;

    /**
     * File header. Formatted to be 8 bytes (hoping it will help alignment).
     * The C identifies this is for 24 *C*lock.
     * The 1 can serve has a format version number in case we want to have future versions.
     */
    private static final byte[] HEADER = new byte[] {
        'P', 'R', 'E', 'F',
        '-', 'C', '1', '\0'};

    private final SerialKey mKeyer = new SerialKey();
    private final RSparseArray<Object> mData = new RSparseArray<Object>();
    private final String mFilename;
    private volatile boolean mDataChanged;
    private volatile Thread mLoadThread;
    private volatile Thread mSaveThread;
    private volatile boolean mLoadResult;
    private volatile boolean mSaveResult;

    /**
     * Opens a serial prefs for "filename.sprefs" in the app's dir.
     * Caller must still read the file before anything happens.
     *
     * @param filename The end leaf filename. Must not be null or empty.
     *   Must not contain any path separator.
     *   This is not an absolute path -- the actual path will depend on the application package.
     */
    public BasePrefsStorage(String filename) {
        mFilename = filename;
    }

    /**
     * Returns the filename that was given to the constructor.
     * This is not an absolute path -- the actual path will depend on the application package.
     */
    public String getFilename() {
        return mFilename;
    }

    /**
     * Returns true if internal data has changed and it's worth
     * calling {@link #beginWriteAsync(Context)}.
     */
    public boolean hasDataChanged() {
        return mDataChanged;
    }

    /**
     * Returns the absolute file path where the preference file is located.
     * It depends on the given context.
     * <p/>
     * This method is mostly useful for unit tests. Normal usage should have no use for that.
     */
    public File getAbsoluteFile(Context context) {
        final Context appContext = context.getApplicationContext();
        return appContext.getFileStreamPath(mFilename);
    }

    /**
     * Starts reading an existing prefs file asynchronously.
     * Callers <em>must</em> call {@link #endReadAsync()}.
     *
     * @param context The {@link Context} to use.
     */
    public void beginReadAsync(Context context) {

        final Context appContext = context.getApplicationContext();

        Thread t = new Thread() {
            @Override
            public void run() {
                FileInputStream fis = null;
                try {
                    fis = appContext.openFileInput(mFilename);
                    mLoadResult = loadChannel(fis.getChannel());
                } catch (FileNotFoundException e) {
                    // This is an expected error.
                    if (DEBUG) Log.d(TAG, "fileNotFound");
                    mLoadResult = true;
                } catch (Exception e) {
                    if (DEBUG) Log.e(TAG, "endReadAsync failed", e);
                } finally {
                    try {
                        if (fis != null) fis.close();
                    } catch (IOException ignore) {}
                }
            }
        };

        synchronized(this) {
            Thread curr = mLoadThread;
            if (curr != null) {
                if (DEBUG) Log.d(TAG, "Load already pending.");
                return;
            } else {
                mLoadThread = t;
                t.start();
            }
        }
    }

    /**
     * Makes sure the asynchronous read has finished.
     * Callers must call this at least once before they access
     * the underlying storage.
     * @return The result from the last load operation:
     *   True if the file was correctly read OR if the file doesn't exist.
     *   False if the false exists and wasn't correctly read.
     */
    public boolean endReadAsync() {
        Thread t = null;
        synchronized(this) {
            t = mLoadThread;
            if (t != null) mLoadThread = null;
        }
        if (t != null) {
            try {
                t.join();
            } catch (InterruptedException e) {
                if (DEBUG) Log.w(TAG, e);
            }
        }
        return mLoadResult;
    }

    /**
     * Saves the prefs if they have changed.
     *
     * @param context The app context.
     * @return True if prefs could be written, false otherwise.
     */
    public boolean writeSync(Context context) {
        if (!mDataChanged) {
            return true;
        }

        beginWriteAsync(context);
        return endWriteAsync();
    }

    /**
     * Starts saving an existing prefs file asynchronously.
     * Nothing happens if there's no data changed and there's already a pending save operation.
     *
     * @param context The {@link Context} to use.
     */
    public void beginWriteAsync(Context context) {
        if (!mDataChanged) {
            return;
        }

        final Context appContext = context.getApplicationContext();

        Thread t = new Thread() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    fos = appContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
                    synchronized(mData) {
                        saveChannel(fos.getChannel());
                        mDataChanged = false;
                    }

                    try {
                        // Notify the backup manager that data might have changed
                        new BackupWrapper(appContext).dataChanged();
                    } catch(Exception ignore) {}

                    mSaveResult = true;
                } catch (Throwable t) {
                    mSaveResult = false;
                    if (DEBUG) Log.e(TAG, "flushSync failed", t);
                    if (THROW_EXCEPTIONS_WHEN_TESTING) throw new RuntimeException(t);
                } finally {
                    synchronized(BasePrefsStorage.this) {
                        mSaveThread = null;
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException ignore) {}
                }
            }
        };

        synchronized(this) {
            Thread curr = mSaveThread;
            if (curr != null) {
                if (DEBUG) Log.d(TAG, "Save already pending.");
                return;
            } else {
                mSaveThread = t;
                t.start();
            }
        }
    }

    /**
     * Makes sure the asynchronous save has finished.
     *
     * @return The result from the last save operation:
     *   True if the file was correctly saved.
     */
    public boolean endWriteAsync() {
        Thread t = null;
        synchronized(this) {
            t = mSaveThread;
        }
        if (t != null) {
            try {
                t.join();
            } catch (InterruptedException e) {
                if (DEBUG) Log.w(TAG, e);
            }
        }
        return mSaveResult;
    }

    // --- put

    public void putInt(String key, int value) {
        synchronized(mData) {
            int intKey = mKeyer.encodeNewKey(key);
            Integer newVal = Integer.valueOf(value);
            Object  curVal = mData.get(intKey);
            if (!newVal.equals(curVal)) {
                mData.put(intKey, newVal);
                mDataChanged = true;
            }
        }
    }

    public void putLong(String key, long value) {
        synchronized(mData) {
            int intKey = mKeyer.encodeNewKey(key);
            Long   newVal = Long.valueOf(value);
            Object curVal = mData.get(intKey);
            if (!newVal.equals(curVal)) {
                mData.put(intKey, newVal);
                mDataChanged = true;
            }
        }
    }

    public void putBool(String key, boolean value) {
        synchronized(mData) {
            int intKey = mKeyer.encodeNewKey(key);
            Boolean newVal = Boolean.valueOf(value);
            Object  curVal = mData.get(intKey);
            if (!newVal.equals(curVal)) {
                mData.put(intKey, newVal);
                mDataChanged = true;
            }
        }
    }

    public void putString(String key, String value) {
        synchronized(mData) {
            int intKey = mKeyer.encodeNewKey(key);
            Object  curVal = mData.get(intKey);
            if ((value == null && curVal != null) || (value != null && !value.equals(curVal))) {
                mData.put(intKey, value);
                mDataChanged = true;
            }
        }
    }

    // --- has

    public boolean hasKey(String key) {
        synchronized(mData) {
            return mData.indexOfKey(mKeyer.encodeKey(key)) >= 0;
        }
    }

    public boolean hasInt(String key) {
        Object o = null;
        synchronized(mData) {
            o = mData.get(mKeyer.encodeKey(key));
        }
        return o instanceof Integer;
    }

    public boolean hasLong(String key) {
        Object o = null;
        synchronized(mData) {
            o = mData.get(mKeyer.encodeKey(key));
        }
        return o instanceof Long;
    }

    public boolean hasBool(String key) {
        Object o = null;
        synchronized(mData) {
            o = mData.get(mKeyer.encodeKey(key));
        }
        return o instanceof Boolean;
    }

    public boolean hasString(String key) {
        Object o = null;
        synchronized(mData) {
            o = mData.get(mKeyer.encodeKey(key));
        }
        return o instanceof String;
    }

    // --- get

    public int getInt(String key, int defValue) {
        if (endReadAsync()) {
            Object o = null;
            synchronized(mData) {
                o = mData.get(mKeyer.encodeKey(key));
            }
            if (o instanceof Integer) {
                return ((Integer) o).intValue();
            } else if (o != null) {
                throw new TypeMismatchException(key, "int", o);
            }
        }
        return defValue;
    }

    public long getLong(String key, long defValue) {
        return getLong(key, defValue, true); // default is to convert int-to-long
    }

    public long getLong(String key, long defValue, boolean convertIntToLong) {
        if (endReadAsync()) {
            Object o = null;
            int intKey = mKeyer.encodeKey(key);
            synchronized(mData) {
                o = mData.get(intKey);
            }
            if (o instanceof Long) {
                return ((Long) o).longValue();
            } else if (o instanceof Integer && convertIntToLong) {
                return ((Integer) o).intValue();
            } else if (o != null) {
                throw new TypeMismatchException(key, "int or long", o);
            }
        }
        return defValue;
    }

    public boolean getBool(String key, boolean defValue) {
        if (endReadAsync()) {
            Object o = null;
            synchronized(mData) {
                o = mData.get(mKeyer.encodeKey(key));
            }
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue();
            } else if (o != null) {
                throw new TypeMismatchException(key, "boolean", o);
            }
        }
        return defValue;
    }

    public String getString(String key, String defValue) {
        if (endReadAsync()) {
            Object o = null;
            synchronized(mData) {
                o = mData.get(mKeyer.encodeKey(key));
            }
            if (o instanceof String) {
                return (String) o;
            } else if (o != null) {
                throw new TypeMismatchException(key, "String", o);
            }
        }
        return defValue;
    }

    // ----

    @PublicForTesting
    protected boolean loadChannel(FileChannel fileChannel) throws IOException {
        // Size should be a multiple of 4. Always.
        // assert (Integer.SIZE / 8) == 4;
        long n = fileChannel.size();
        if (n < HEADER.length || (n & 0x03) != 0) {
            Log.d(TAG, "Invalid file size, should be multiple of 4.");
            return false;
        }

        assert (HEADER.length & 0x03) == 0;
        ByteBuffer header = ByteBuffer.allocate(HEADER.length);
        header.order(ByteOrder.LITTLE_ENDIAN);
        int r = fileChannel.read(header);
        if (r != HEADER.length || !Arrays.equals(HEADER, header.array())) {
            Log.d(TAG, "Invalid file format, wrong header.");
            return false;
        }
        n -= r;
        header = null;

        if (n > Integer.MAX_VALUE) {
            Log.d(TAG, "Invalid file size, file is too large.");
            return false;
        }

        // read all data
        ByteBuffer bytes = ByteBuffer.allocateDirect((int) n);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        r = fileChannel.read(bytes);
        if (r != n) {
            Log.d(TAG, "Failed to read all data.");
            return false;
        }

        // convert to an int buffer
        int[] data = new int[bytes.capacity() / 4];
        bytes.position(0); // rewind and read
        bytes.asIntBuffer().get(data);

        SerialReader sr = new SerialReader(data);

        synchronized(mData) {
            mData.clear();

            for (SerialReader.Entry entry : sr) {
                mData.append(entry.getKey(), entry.getValue());
            }
            mDataChanged = false;
        }

        return true;
    }

    @PublicForTesting
    protected void saveChannel(FileChannel fileChannel) throws IOException {
        BufferedWriter bw = null;
        try {
            ByteBuffer header = ByteBuffer.wrap(HEADER);
            header.order(ByteOrder.LITTLE_ENDIAN);
            if (fileChannel.write(header) != HEADER.length) {
                throw new IOException("Failed to write header.");
            }

            SerialWriter sw = new SerialWriter();

            for (int n = mData.size(), i = 0; i < n; i++) {
                int key = mData.keyAt(i);
                Object value = mData.valueAt(i);

                // no need to store null values.
                if (value == null) continue;

                if (value instanceof Integer) {
                    sw.addInt(key, ((Integer) value).intValue());
                } else if (value instanceof Long) {
                    sw.addLong(key, ((Long) value).longValue());
                } else if (value instanceof Boolean) {
                    sw.addBool(key, ((Boolean) value).booleanValue());
                } else if (value instanceof String) {
                    sw.addString(key, (String) value);
                } else {
                    throw new UnsupportedOperationException(
                            this.getClass().getSimpleName() +
                            " does not support type " +
                            value.getClass().getSimpleName());
                }
            }

            int[] data = sw.encodeAsArray();
            ByteBuffer bytes = ByteBuffer.allocateDirect(data.length * 4);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.asIntBuffer().put(data);
            fileChannel.write(bytes);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignore) {}
            }
        }
    }

    public static class TypeMismatchException extends RuntimeException {
        private static final long serialVersionUID = -6386235026748640081L;
        public TypeMismatchException(String key, String expected, Object actual) {
            super(String.format("Key '%1$s' excepted type %2$s, got %3$s",
                    key, expected, actual.getClass().getSimpleName()));
        }
    }

}
