/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.Display;
import android.view.View;

import com.alflabs.annotations.NonNull;

import java.util.concurrent.atomic.AtomicInteger;


public class ApiHelper {

    private static ApiHelper sApiHelper = null;

    /** Creates a new ApiHelper adapted to the current runtime API level. */
    public static ApiHelper get() {
        if (sApiHelper == null) {
            int apiLevel = Utils.getApiLevel();

            if (apiLevel >= 19) {
                sApiHelper = new ApiHelper_19();

            } else if (apiLevel >= 18) {
                sApiHelper = new ApiHelper_18();

            } else if (apiLevel >= 17) {
                sApiHelper = new ApiHelper_17();

            } else if (apiLevel >= 16) {
                sApiHelper = new ApiHelper_16();

            } else if (apiLevel >= 13) {
                sApiHelper = new ApiHelper_13();

            } else if (apiLevel >= 11) {
                sApiHelper = new ApiHelper_11();

            } else if (apiLevel >= 9) {
                sApiHelper = new ApiHelper_09();

            } else {
                sApiHelper = new ApiHelper();
            }
        }

        return sApiHelper;
    }

    protected ApiHelper() {
    }

    /**
     * Workaround for {@link Configuration#screenWidthDp}, defined only starting with API 13.
     * It just returns 320 for API < 13
     */
    public int Context_getScreenWidth(Context appContext) {
        return 320;
    }

    /**
     * Applies {@link View#setSystemUiVisibility(int)}, available only starting with API 11.
     * Does nothing for API < 11.
     */
    public void View_setSystemUiVisibility(View view, int visibility) {
        // nop
    }

    /**
     * Applies {@link View#setAlpha(float)}, available only starting with API 11.
     * Does nothing for API < 11.
     */
    public void View_setAlpha(View view, float alpha) {
        // nop
    }

    /**
     * Returns {@link View#generateViewId()}, available only starting with API 17.
     *
     * Generate a value suitable for use in View.setId(int).
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * Implemented following Android 19's source.
     * @return a generated ID value
     */
    public int View_generateViewId() {
        if (sNextGeneratedId == null) {
            sNextGeneratedId = new AtomicInteger(1);
        }

        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) {
                newValue = 1; // Roll over to 1, not 0.
            }
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    private static AtomicInteger sNextGeneratedId = null;

    /**
     * Calls {@link View#setBackground(Drawable)} or {@link View#setBackgroundDrawable(Drawable)}.
     * The former is for API 16+, the later is for older API before 16. They do the same thing.
     */
    public void View_setBackground(View view, Drawable background) {
        //noinspection deprecation
        view.setBackgroundDrawable(background);
    }

    /**
     * Returns the {@link android.view.Display#getSize} with is defined starting with API 13.
     * For API 10, uses {@link android.view.Display#getWidth} and {@link android.view.Display#getHeight}
     */
    public Point Display_getSize(Display display) {
        return new Point(display.getWidth(),  display.getHeight());
    }

    /**
     * Calls Looper.quitSafely() on API 18+.
     * Calls the regular Looper.quit() below API 18.
     */
    public void Looper_quitSafely(@NonNull Looper looper) {
        looper.quit();
    }
    /**
     * Returns Activity.isDestroyed() for API 17+.
     * Always returns false for API < 17.
     */
    public boolean Activity_isDestroyed(@NonNull Activity activity) {
        return false;
    }

    /**
     * Calls SharedPreferences.Editor.apply() on API 9+.
     * Calls SharedPreferences.Editor.commit() on API < 9 but does not return any result.
     */
    public void SharedPreferences_Editor_apply(@NonNull SharedPreferences.Editor editor) {
        editor.apply();
    }

    /**
     * Calls ActivityManager.isLowRamDevice() on API 19+.
     * Returns true below API 19.
     */
    public boolean ActivityManager_isLowRamDevice(@NonNull ActivityManager activityManager) {
        return true;
    }

}
