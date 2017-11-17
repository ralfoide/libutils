/*
 * Project: Lib Utils
 * Copyright (C) 2017 alf.labs gmail com,
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

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

/**
 * Implementation of {@link ILogger} that outputs to a string.
 */
public class StringLogger implements ILogger {

    private final StringBuilder mStringBuilder = new StringBuilder();

    @Override
    public void d(@NonNull String tag, @NonNull String message) {
        mStringBuilder.append(tag).append(": ").append(message).append('\n');
    }

    @Override
    public void d(@NonNull String tag, @NonNull String message, @Null Throwable tr) {
        mStringBuilder.append(tag).append(": ").append(message).append(": ").append(tr).append('\n');
    }

    public String getString() {
        return mStringBuilder.toString();
    }

    public void clear() {
        mStringBuilder.setLength(0);
    }
}
