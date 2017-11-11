/*
 * Project: Lib Utils
 * Copyright (C) 2014 alf.labs gmail com,
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
