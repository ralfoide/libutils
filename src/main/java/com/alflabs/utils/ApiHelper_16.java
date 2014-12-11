/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.utils;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * API 16: support View_setBackground
 */
@TargetApi(16)
class ApiHelper_16 extends ApiHelper_13 {

    /**
     * Calls {@link View#setBackground(android.graphics.drawable.Drawable)} or {@link View#setBackgroundDrawable(android.graphics.drawable.Drawable)}.
     * The former is for API 16+, the later is for older API before 16. They do the same thing.
     * @param background
     */
    public void View_setBackground(View view, Drawable background) {
        view.setBackground(background);
    }
}
