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


package com.alflabs.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Log;

import com.alflabs.annotations.NonNull;
import com.alflabs.app.ICoreStrings;
import com.alflabs.libutils.BuildConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Utils {

    public static final String TAG = Utils.class.getSimpleName();
    public static final boolean DEBUG = BuildConfig.DEBUG;

    private static int sSdkInt = 0;

    /** Returns current API level. The API level value is then cached. */
    @SuppressWarnings("deprecation")
    public static int getApiLevel() {
        if (sSdkInt > 0) return sSdkInt;

        // Build.SDK_INT is only in API 4 and we're still compatible with API 3
        try {
            int n = Integer.parseInt(Build.VERSION.SDK);
            sSdkInt = n;
            return n;
        } catch (Exception e) {
            Log.d(TAG, "Failed to parse Build.VERSION.SDK=" + Build.VERSION.SDK, e);
            return 3;
        }
    }

    /** Returns true if current API Level is equal or greater than {@code minApiLevel}. */
    public static boolean checkMinApiLevel(int minApiLevel) {
        return getApiLevel() >= minApiLevel;
    }

    /**
     * Indicates whether the current platform reasonably looks like an emulator.
     * This could break if the emulator reported different values or if a custom
     * ROM was providing these values (e.g. one based on an emulator image.)
     */
    public static boolean isEmulator() {
        // On the emulator:
        // Build.BRAND = generic
        // Build.DEVICE = generic
        // Build.MODEL = sdk
        // Build.PRODUCT = sdk or google_sdk (for the addon)
        // Build.MANUFACTURER = unknown -- API 4+
        // Build.HARDWARE = goldfish -- API 8+

        String b = Build.BRAND;     // "generic" or "generic_x86"
        String d = Build.DEVICE;    // "generic" or "generic_x86"
        String p = Build.PRODUCT;   // "sdk_phone", "google_sdk", "sdk_phone_x86", "google_sdk_x86"

        //noinspection RedundantIfStatement
        if (b != null && b.contains("generic") &&
                d != null && d.contains("generic") &&
                p != null && p.contains("sdk")) {
            // Most likely an emulator
            return true;
        }

        return false;
    }

    /**
     * Returns true if the current app is signed with my specific debug key.
     */
    public static boolean isUsingDebugKey(Context context, ICoreStrings strings) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                            context.getPackageName(),
                            PackageManager.GET_SIGNATURES);

            Signature[] sigs = pi.signatures;
            if (sigs == null || sigs.length != 1) {
                return false;
            }

            Signature sig = pi.signatures[0];
            String str = sig.toCharsString();

            String start = strings.get(ICoreStrings.DEBUG_KEY1);
            String end   = strings.get(ICoreStrings.DEBUG_KEY2);

            if (start.length() == 0) {
                int hash = sig.hashCode();
                Log.d(TAG, String.format("Sig [%08x]: %s", hash, str));
            } else if (str != null &&
                            str.startsWith(start) &&
                            str.endsWith(end)) {
                Log.d(TAG, "Using Debug Key");
                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "UsingKey exception: " + e.toString());
        }
        return false;
    }

    /**
     * Serializes a Java Serializable object to a string container.
     * The string is only guaranteed to be compatible with {@link #deserializeFromString}
     */
    @NonNull
    public static String serializeToString(@NonNull Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();

        byte[] bytes = baos.toByteArray();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int c = (b >> 4) & 0x0F;
            sb.append((char)(c < 10 ? ('0' + c) : ('a' + c - 10)));
            c = b & 0x0F;
            sb.append((char)(c < 10 ? ('0' + c) : ('a' + c - 10)));
        }

        return sb.toString();
    }

    /**
     * Deserializes an object from a string previous returned by {@link #serializeToString(Object)}.
     *
     * @param serializedString
     * @return Possibly something vaguely like the original object. Somewhat. Maybe. Kinda. Sorta.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object deserializeFromString(@NonNull String serializedString) throws IOException, ClassNotFoundException {
        int n = serializedString.length() / 2;
        byte[] bytes = new byte[n];
        for (int i = 0, k = 0; i < n; i++, k+=2) {
            char c = serializedString.charAt(k);
            byte b = (byte) ((c < 'a' ? (c - '0') : (c + 10 - 'a')) << 4);

                 c = serializedString.charAt(k+1);
                b += (byte) ((c < 'a' ? (c - '0') : (c + 10 - 'a')));
            bytes[i] = b;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object object = ois.readObject();
        ois.close();
        return object;
    }
}
