/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.utils;

import android.annotation.TargetApi;
import android.view.View;

/**
 * API 11: support View_setSystemUiVisibility
 */
@TargetApi(11)
class ApiHelper_11 extends ApiHelper {

    /**
     * Applies {@link View#setSystemUiVisibility(int)}, available only starting with API 11.
     * Does nothing for API < 11.
     */
    @Override
    public void View_setSystemUiVisibility(View view, int visibility) {
        view.setSystemUiVisibility(visibility);
    }

    /**
     * Applies {@link View#setAlpha(float)}, available only starting with API 11.
     * Does nothing for API < 11.
     */
    @Override
    public void View_setAlpha(View view, float alpha) {
        view.setAlpha(alpha);
    }
}
