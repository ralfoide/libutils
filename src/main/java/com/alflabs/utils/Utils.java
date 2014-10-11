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

    // My debug key signature is
    //  30820255308201bea00302010202044a88ef41300d06092a864886f70d0101050500306f310b30090603550406130255533110300e06035504081307556e6b6e6f776e3110300e06035504071307556e6b6e6f776e310d300b060355040a130452616c663110300e060355040b1307556e6b6e6f776e311b301906035504031312416e64726f69642052616c66204465627567301e170d3039303831373035343834395a170d3337303130323035343834395a306f310b30090603550406130255533110300e06035504081307556e6b6e6f776e3110300e06035504071307556e6b6e6f776e310d300b060355040a130452616c663110300e060355040b1307556e6b6e6f776e311b301906035504031312416e64726f69642052616c6620446562756730819f300d06092a864886f70d010101050003818d0030818902818100aafbaa519b7a0cb0accb5cc37b6138b99bde072a952b291fb90cdb067e1f7c1980aac7152aee0304da0eb400d10dd7fef09e9e13f07ea09a3e500c86ba9fb93b5792003817cfd1639a9bffd085aa479521593d5ea516836e2eec5312c9044bafed29cf1339d4960ed96968fcbafd7ddd921b140b0de62f0576afa912789462630203010001300d06092a864886f70d01010505000381810027e6f99c4ef8c02d4dde0d1ec92bad13ea9ce85609e5d5e042e9800aca1e8472563be4fe6463ed1e12a72ff2780599d79ce03130925e345a196316b7ccab9b8941979c41e56c233d1a0a17f342cc74474615e7fde1340aac02c850b6119a708774973487250cd9d32e8c23ec8ed99d0a7ff07046c3ba53d387aa07805f6a8389
    //  hash = 1746973045  0x6820b175
    // (to update this, empty first field and look in the log for the printed signature to match.)
    private static final String DEBUG_KEY_START = "30820255308201bea00302010202044";
    private static final String DEBUG_KEY_END   = "e8c23ec8ed99d0a7ff07046c3ba53d387aa07805f6a8389";

    /**
     * Returns true if the current app is signed with my specific debug key.
     */
    public static boolean isUsingDebugKey(Context context) {
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

            if (DEBUG_KEY_START.length() == 0) {
                int hash = sig.hashCode();
                Log.d(TAG, String.format("Sig [%08x]: %s", hash, str));
            } else if (str != null &&
                            str.startsWith(DEBUG_KEY_START) &&
                            str.endsWith(DEBUG_KEY_END)) {
                Log.d(TAG, "Using Debug Key");
                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "UsingKey exception: " + e.toString());
        }
        return false;
    }
}
