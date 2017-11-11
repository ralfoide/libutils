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

import android.annotation.TargetApi;
import android.view.View;

/**
 * API 11: support View_setSystemUiVisibility
 */
@TargetApi(11)
class ApiHelper_11 extends ApiHelper_09 {

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
