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

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

import java.util.Iterator;
import java.util.zip.CRC32;

/**
 * Encoder/decoder for typed data.
 * Extracted from NerdkillAndroid.
 * <p/>
 *
 * A {@link com.alflabs.serial.SerialWriter} serialized typed key/values to an int array.
 * Each key is identified by a name -- however <em>only</em> the hashcode
 * of the key string is used as an id, not the exact string itself. <br/>
 * An exception {@link com.alflabs.serial.SerialKey.DuplicateKey} is thrown when trying to add a key
 * that has the same string hashcode than an already present key. <br/>
 * The assumption is made that string hashcodes are stable for a given
 * interpreting machine (e.g. JVM or Dalvik).
 * <p/>
 *
 * <p/>
 * Supported types: <br/>
 * - integer        <br/>
 * - long           <br/>
 * - bool           <br/>
 * - float          <br/>
 * - double         <br/>
 * - string         <br/>
 * - {@link com.alflabs.serial.SerialWriter}
 * <p/>
 *
 * The type {@link com.alflabs.serial.SerialWriter} can be used to embedded a struct within
 * a struct.
 * <p/>
 *
 * Users of the serialized data can either directly process the int[] array
 * using {@link #encodeAsArray()} or can transform it to a semi-compact hexa
 * string using {@link #encodeAsString()}. Both can later be decoded using
 * {@link com.alflabs.serial.SerialReader}.
 * <p/>
 *
 * A typical strategy is for a serializable struct to contain a
 * <code>saveInto(SerialWriter)</code> method. Whatever code that does
 * the saving can thus first create a {@link com.alflabs.serial.SerialWriter} and pass it
 * to the struct so that it can save itself into the given writer.
 * <br/>
 * Another strategy is for the struct to have a method that creates the
 * {@link com.alflabs.serial.SerialWriter}, adds all the fields and then return the writer.
 * <br/>
 * Both approaches are valid. The former is useful if the outer and inner
 * methods are working in collaboration, the later is useful if both sides
 * should be agnostic to each other.
 * <p/>
 *
 * Format of the encoded int array:
 * The serialized data starts with a header describing the data size,
 * followed by one entry per field added in the order of the addXyz() calls.
 * Finally there's a CRC and an EOF marker.
 *
 * <pre>
 * - header:
 *   - 1 int: number of ints to follow (including this and CRC + EOF marker)
 * - entries:
 *   - 1 int: type, ascii for I L B F D S or Z
 *   - 1 int: key
 *   - int, bool, float: 1 int, value
 *   - long, double: 2 int value (MSB + LSB)
 *   - string: 1 int = number of chars following, then int = c1 | c2 (0 padding as needed)
 *   - serializer: (self, starting with number of ints to follow)
 * - 1 int: CRC (of all previous ints including header, excluding this and EOF)
 * - 1 int: EOF
 * </pre>
 */
public class SerialWriter {

    public static class CantAddData extends RuntimeException {
        private static final long serialVersionUID = 8074293730213951679L;
        public CantAddData(@Null String message) { super(message); }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private final SerialKey mKeyer = new SerialKey();

    private int[] mData;
    private int mSize;
    private boolean mCantAdd;

    static final int TYPE_INT    = 1;
    static final int TYPE_LONG   = 2;
    static final int TYPE_BOOL   = 3;
    static final int TYPE_FLOAT  = 4;
    static final int TYPE_DOUBLE = 5;
    static final int TYPE_STRING = 6;
    static final int TYPE_SERIAL = 7;
    static final int EOF = 0x0E0F;

    public SerialWriter() {
    }

    /**
     * Recreates a SerialWriter from a SerialReader.
     * This is not efficient. Also order is not preserved.
     * There are often better ways to avoid this.
     */
    public SerialWriter(@NonNull SerialReader reader) {
        for (SerialReader.Entry entry : reader) {
            int key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer) {
                addInt(key, (Integer) value);

            } else if (value instanceof Long) {
                addLong(key, (Long) value);

            } else if (value instanceof Boolean) {
                addBool(key, (Boolean) value);

            } else if (value instanceof Float) {
                addFloat(key, (Float) value);

            } else if (value instanceof Double) {
                addDouble(key, (Double) value);

            } else if (value instanceof String) {
                addString(key, (String) value);

            } else if (value instanceof SerialReader) {
                addSerial(key, new SerialWriter((SerialReader)value));
            }
        }
    }

    @NonNull
    public int[] encodeAsArray() {
        close();

        // Resize the array to be of its exact size
        assert mData != null;
        if (mData.length > mSize) {
            int[] newArray = new int[mSize];
            System.arraycopy(mData, 0, newArray, 0, mSize);
            mData = newArray;
        }

        return mData;
    }

    @NonNull
    public String encodeAsString() {
        int[] a = encodeAsArray();
        int n = a.length;
        char[] cs = new char[n * 9];

        int j = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < n; i++) {
            int v = a[i];

            // Write nibbles in MSB-LSB order, skipping leading zeros
            boolean skipZero = true;
            for (int k = 0; k < 8; k++) {
                int b = (v >> 28) & 0x0F;
                v = v << 4;
                if (skipZero) {
                    if (b == 0) {
                        if (k < 7) {
                            continue;
                        }
                    } else {
                        skipZero = false;
                    }
                }
                char c = b < 0x0A ? (char)('0' + b) : (char)('A' - 0x0A + b);
                cs[j++] = c;
            }

            cs[j++] = ' ';
        }

        return new String(cs, 0, j);
    }

    //--

    /**
     * @throws com.alflabs.serial.SerialKey.DuplicateKey if the key had already been registered.
     */
    public void addInt(@NonNull String name, int intValue) {
        addInt(mKeyer.encodeUniqueKey(name), intValue);
    }

    /**
     * @throws com.alflabs.serial.SerialKey.DuplicateKey if the key had already been registered.
     */
    public void addLong(@NonNull String name, long longValue) {
        addLong(mKeyer.encodeUniqueKey(name), longValue);
    }

    /**
     * @throws com.alflabs.serial.SerialKey.DuplicateKey if the key had already been registered.
     */
    public void addBool(@NonNull String name, boolean boolValue) {
        addBool(mKeyer.encodeUniqueKey(name), boolValue);
    }

    /**
     * @throws com.alflabs.serial.SerialKey.DuplicateKey if the key had already been registered.
     */
    public void addFloat(@NonNull String name, float floatValue) {
        addFloat(mKeyer.encodeUniqueKey(name), floatValue);
    }

    /**
     * @throws com.alflabs.serial.SerialKey.DuplicateKey if the key had already been registered.
     */
    public void addDouble(@NonNull String name, double doubleValue) {
        addDouble(mKeyer.encodeUniqueKey(name), doubleValue);
    }

    /** Add a string. Doesn't add a null value.
     * @throws com.alflabs.serial.SerialKey.DuplicateKey if the key had already been registered.
     */
    public void addString(@NonNull String name, @NonNull String strValue) {
        addString(mKeyer.encodeUniqueKey(name), strValue);
    }

    /** Add a Serial. Doesn't add a null value. */
    public void addSerial(@NonNull String name, @NonNull SerialWriter serialValue) {
        addSerial(mKeyer.encodeUniqueKey(name), serialValue);
    }

    //--

    public void addInt(int key, int intValue) {
        _addInt(key, TYPE_INT, intValue);
    }

    public void addLong(int key, long longValue) {
        _addLong(key, TYPE_LONG, longValue);
    }

    public void addBool(int key, boolean boolValue) {
        _addInt(key, TYPE_BOOL, boolValue ? 1 : 0);
    }

    public void addFloat(int key, float floatValue) {
        _addInt(key, TYPE_FLOAT, Float.floatToIntBits(floatValue));
    }

    public void addDouble(int key, double doubleValue) {
        _addLong(key, TYPE_DOUBLE, Double.doubleToLongBits(doubleValue));
    }

    /** Add a string. Doesn't add a null value. */
    public void addString(int key, @NonNull String strValue) {
        //noinspection ConstantConditions
        if (strValue == null) return;

        int n = strValue.length();
        int m = (n + 1) / 2;

        int pos = alloc(2 + m + 1);
        mData[pos++] = TYPE_STRING;
        mData[pos++] = key;
        mData[pos++] = n;

        for (int i = 0; i < n;) {
            char c1 = strValue.charAt(i++);
            char c2 = i >= n ? 0 : strValue.charAt(i++);
            int j = (c1 << 16) | (c2 & 0x0FFFF);
            mData[pos++] = j;
        }

        mSize = pos;
    }

    /** Add a Serial. Doesn't add a null value. */
    public void addSerial(int key, @NonNull int[] encodedDserialValue) {
        //noinspection ConstantConditions
        if (encodedDserialValue == null) return;

        int n = encodedDserialValue.length;

        int pos = alloc(2 + n);
        mData[pos++] = TYPE_SERIAL;
        mData[pos++] = key;

        System.arraycopy(encodedDserialValue, 0, mData, pos, n);
        pos += n;

        mSize = pos;
    }

    /** Add a Serial. Doesn't add a null value. */
    public void addSerial(int key, @NonNull SerialWriter serialValue) {
        //noinspection ConstantConditions
        if (serialValue == null) return;
        addSerial(key, serialValue.encodeAsArray());
    }

    //---

    private int alloc(int numInts) {
        if (mCantAdd) {
            throw new CantAddData("Serial data has been closed by a call to encode(). Can't add anymore.");
        }

        if (mData == null) {
            // Reserve the header int
            mData = new int[numInts + 1];
            mSize = 1;
            return mSize;
        }

        int s = mSize;
        int n = s + numInts;

        if (mData.length < n) {
            // need to realloc
            int[] newArray = new int[s+n];
            System.arraycopy(mData, 0, newArray, 0, s);
            mData = newArray;
        }
        return mSize;
    }

    private void _addInt(int key, int type, int value) {
        int pos = alloc(2+1);
        mData[pos++] = type;
        mData[pos++] = key;
        mData[pos++] = value;
        mSize = pos;
    }

    private void _addLong(int key, int type, long value) {
        int pos = alloc(2+2);
        mData[pos++] = type;
        mData[pos++] = key;
        // MSB first
        mData[pos++] = (int) (value >> 32);
        // LSB next
        //noinspection PointlessBitwiseExpression
        mData[pos++] = (int) (value & 0xFFFFFFFF);
        mSize = pos;
    }

    /** Closing the array adds the CRC and the EOF. Can't add anymore once this is done. */
    private void close() {
        // can't close the array twice
        if (mCantAdd) return;

        int pos = alloc(2);

        // write the header, the first int is the full size in ints
        mData[0] = pos + 2;
        // write the CRC and EOF footer
        mData[pos  ] = computeCrc(mData, 0, pos);
        mData[pos+1] = EOF;
        mSize += 2;
        mCantAdd = true;
    }

    /* This is static so that we can reuse it as-is in the reader class. */
    static int computeCrc(int[] data, int offset, int length) {
        CRC32 crc = new CRC32();

        for (; length > 0; length--) {
            int v = data[offset++];
            crc.update(v & 0x0FF);
            v = v >> 8;
            crc.update(v & 0x0FF);
            v = v >> 8;
            crc.update(v & 0x0FF);
            v = v >> 8;
            crc.update(v & 0x0FF);
        }

        return (int) crc.getValue();
    }
}
