/*
 * Project: Set Sample
 * Copyright (C) 2013 alf.labs gmail com,
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

package com.alflabs.serial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import android.content.Context;
import android.util.Log;
import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.annotations.PublicForTesting;
import com.alflabs.utils.RSparseArray;

/**
 * Wrapper around {@link SerialWriter} and {@link SerialReader} to deal with async data.
 * <p/>
 * Supported types are the minimal required for hour needs: boolean, string, int, long and serial.
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
public class AsyncSerialFile {

    private static final boolean DEBUG = false;
    public static final String TAG = AsyncSerialFile.class.getSimpleName();

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
        'S', 'E', 'R', 'I', 'A', 'L', '2', '\0'};

    @SuppressWarnings("SpellCheckingInspection")
    private final SerialKey mKeyer = new SerialKey();
    private final RSparseArray<Object> mData = new RSparseArray<Object>();
    private final @NonNull String mFilename;
    private boolean mDataChanged;
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
    public AsyncSerialFile(@NonNull String filename) {
        mFilename = filename;
    }

    /**
     * Returns the filename that was given to the constructor.
     * This is not an absolute path -- the actual path will depend on the application package.
     */
    @NonNull
    public String getFilename() {
        return mFilename;
    }

    /**
     * Returns the absolute file path where the preference file is located.
     * It depends on the given context.
     * <p/>
     * This method is mostly useful for unit tests. Normal usage should have no use for that.
     */
    public File getAbsoluteFile(Context context) {
        final Context appContext = context.getApplicationContext();
        assert appContext != null;
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
        assert appContext != null;

        Thread t = new Thread() {
            @Override
            public void run() {
                FileInputStream fis = null;
                try {
                    fis = appContext.openFileInput(mFilename);
                    mLoadResult = loadChannel(fis.getChannel());
                } catch (FileNotFoundException e) {
                    // This is an expected error.
                    if (DEBUG) Log.d(TAG, "fileNotFound");                  //NLS
                    mLoadResult = true;
                } catch (Exception e) {
                    if (DEBUG) Log.e(TAG, "endReadAsync failed", e);        //NLS
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
                if (DEBUG) Log.d(TAG, "Load already pending.");             //NLS
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
        assert appContext != null;

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
                    mSaveResult = true;
                } catch (Throwable t) {
                    mSaveResult = false;
                    if (DEBUG) Log.e(TAG, "flushSync failed", t);                       //NLS
                    if (THROW_EXCEPTIONS_WHEN_TESTING) throw new RuntimeException(t);
                } finally {
                    synchronized(AsyncSerialFile.this) {
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
                if (DEBUG) Log.d(TAG, "Save already pending.");                         //NLS
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

    @SuppressWarnings("UnnecessaryBoxing")
    public void putInt(@NonNull String key, int value) {
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

    @SuppressWarnings("UnnecessaryBoxing")
    public void putLong(@NonNull String key, long value) {
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

    @SuppressWarnings("UnnecessaryBoxing")
    public void putBool(@NonNull String key, boolean value) {
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

    public void putString(@NonNull String key, @NonNull String value) {
        synchronized(mData) {
            int intKey = mKeyer.encodeNewKey(key);
            Object  curVal = mData.get(intKey);
            if ((value == null && curVal != null) || (value != null && !value.equals(curVal))) {
                mData.put(intKey, value);
                mDataChanged = true;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void putSerial(@NonNull String key, @NonNull SerialWriter value) {
        synchronized(mData) {
            int intKey = mKeyer.encodeNewKey(key);
            Object  curVal = mData.get(intKey);
            if ((value == null && curVal != null) || (value != null && !value.equals(curVal))) {
                mData.put(intKey, value.encodeAsArray());
                mDataChanged = true;
            }
        }
    }

    // --- has

    public boolean hasKey(@NonNull String key) {
        synchronized(mData) {
            return mData.indexOfKey(mKeyer.encodeKey(key)) >= 0;
        }
    }

    public boolean hasInt(@NonNull String key) {
        Object o = null;
        synchronized(mData) {
            o = mData.get(mKeyer.encodeKey(key));
        }
        return o instanceof Integer;
    }

    public boolean hasLong(@NonNull String key) {
        Object o = null;
        synchronized(mData) {
            o = mData.get(mKeyer.encodeKey(key));
        }
        return o instanceof Long;
    }

    public boolean hasBool(@NonNull String key) {
        Object o = null;
        synchronized(mData) {
            o = mData.get(mKeyer.encodeKey(key));
        }
        return o instanceof Boolean;
    }

    public boolean hasString(@NonNull String key) {
        Object o = null;
        synchronized(mData) {
            o = mData.get(mKeyer.encodeKey(key));
        }
        return o instanceof String;
    }

    public boolean hasSerial(@NonNull String key) {
        Object o = null;
        synchronized(mData) {
            o = mData.get(mKeyer.encodeKey(key));
        }
        return o instanceof int[] || o instanceof SerialWriter || o instanceof SerialReader;
    }

    // --- get

    @SuppressWarnings("UnnecessaryUnboxing")
    public int getInt(@NonNull String key, int defValue) {
        if (endReadAsync()) {
            Object o = null;
            synchronized(mData) {
                o = mData.get(mKeyer.encodeKey(key));
            }
            if (o instanceof Integer) {
                return ((Integer) o).intValue();
            } else if (o != null) {
                throw new TypeMismatchException(key, "int", o);                 //NLS
            }
        }
        return defValue;
    }

    @SuppressWarnings("UnnecessaryUnboxing")
    public long getLong(@NonNull String key, long defValue) {
        return getLong(key, defValue, true); // default is to convert int-to-long
    }

    @SuppressWarnings("UnnecessaryUnboxing")
    public long getLong(@NonNull String key, long defValue, boolean convertIntToLong) {
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
                throw new TypeMismatchException(key, "int or long", o);         //NLS
            }
        }
        return defValue;
    }

    @SuppressWarnings("UnnecessaryUnboxing")
    public boolean getBool(@NonNull String key, boolean defValue) {
        if (endReadAsync()) {
            Object o = null;
            synchronized(mData) {
                o = mData.get(mKeyer.encodeKey(key));
            }
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue();
            } else if (o != null) {
                throw new TypeMismatchException(key, "boolean", o);             //NLS
            }
        }
        return defValue;
    }

    @Null
    @SuppressWarnings("UnnecessaryUnboxing")
    public String getString(@NonNull String key, @Null String defValue) {
        if (endReadAsync()) {
            Object o = null;
            synchronized(mData) {
                o = mData.get(mKeyer.encodeKey(key));
            }
            if (o instanceof String) {
                return (String) o;
            } else if (o != null) {
                throw new TypeMismatchException(key, "String", o);              //NLS
            }
        }
        return defValue;
    }

    @Null
    @SuppressWarnings("UnnecessaryUnboxing")
    public SerialReader getSerial(@NonNull String key) {
        if (endReadAsync()) {
            Object o = null;
            synchronized(mData) {
                o = mData.get(mKeyer.encodeKey(key));
            }
            if (o instanceof int[]) {
                return new SerialReader((int[]) o);
            } else if (o instanceof SerialReader) {
                return (SerialReader) o;
            } else if (o instanceof SerialWriter) {
                return new SerialReader((SerialWriter) o);
            } else if (o != null) {
                throw new TypeMismatchException(key, "Serial", o);              //NLS
            }
        }
        return null;
    }

    // ----

    @PublicForTesting
    protected boolean loadChannel(FileChannel fileChannel) throws IOException {
        // Size should be a multiple of 4. Always.
        // assert (Integer.SIZE / 8) == 4;
        long n = fileChannel.size();
        if (n < HEADER.length || (n & 0x03) != 0) {
            Log.d(TAG, "Invalid file size, should be multiple of 4.");                  //NLS
            return false;
        }

        assert (HEADER.length & 0x03) == 0;
        ByteBuffer header = ByteBuffer.allocate(HEADER.length);
        header.order(ByteOrder.LITTLE_ENDIAN);
        int r = fileChannel.read(header);
        if (r != HEADER.length || !Arrays.equals(HEADER, header.array())) {
            Log.d(TAG, "Invalid file format, wrong header.");                           //NLS
            return false;
        }
        n -= r;
        header = null;

        if (n > Integer.MAX_VALUE) {
            Log.d(TAG, "Invalid file size, file is too large.");                        //NLS
            return false;
        }

        // read all data
        ByteBuffer bytes = ByteBuffer.allocateDirect((int) n);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        r = fileChannel.read(bytes);
        if (r != n) {
            Log.d(TAG, "Failed to read all data.");                                     //NLS
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

    @SuppressWarnings("UnnecessaryUnboxing")
    @PublicForTesting
    protected void saveChannel(FileChannel fileChannel) throws IOException {
        ByteBuffer header = ByteBuffer.wrap(HEADER);
        header.order(ByteOrder.LITTLE_ENDIAN);
        if (fileChannel.write(header) != HEADER.length) {
            throw new IOException("Failed to write header.");                           //NLS
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
            } else if (value instanceof int[]) {
                sw.addSerial(key, (int[]) value);
            } else {
                throw new UnsupportedOperationException(
                        this.getClass().getSimpleName() +
                        " does not support type " +                                     //NLS
                        value.getClass().getSimpleName());
            }
        }

        int[] data = sw.encodeAsArray();
        ByteBuffer bytes = ByteBuffer.allocateDirect(data.length * 4);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.asIntBuffer().put(data);
        fileChannel.write(bytes);
    }

    public static class TypeMismatchException extends RuntimeException {
        private static final long serialVersionUID = -6386235026748640081L;
        public TypeMismatchException(@NonNull String key, @NonNull String expected, @NonNull Object actual) {
            super(String.format("Key '%1$s' excepted type %2$s, got %3$s",              //NLS
                    key, expected, actual.getClass().getSimpleName()));
        }
    }

}
