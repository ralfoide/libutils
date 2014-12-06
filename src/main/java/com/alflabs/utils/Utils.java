/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Log;
import com.alflabs.app.ICoreStrings;


public class Utils {

    public static final String TAG = Utils.class.getSimpleName();
    public static final boolean DEBUG = true;

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

        String b = Build.BRAND;
        String d = Build.DEVICE;
        String p = Build.PRODUCT;
        String h = ""; // RM TODO ApiHelper.get().Build_HARDWARE();

        if ("generic".equals(b) &&
                "generic".equals(d) &&
                (h == null || "goldfish".equals(h)) &&
                ("sdk".equals(p) || "google_sdk".equals(p))) {
            Log.w(TAG, "Emulator Detected. Stats disabled.");
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
}
