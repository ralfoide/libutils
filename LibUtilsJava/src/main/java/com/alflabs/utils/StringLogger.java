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
 * Implementation of {@link ILogger} that outputs to a string and optionally also outputs to
 * {@link System#out}.
 */
public class StringLogger implements ILogger {

    private final boolean mSysOut;
    private final StringBuilder mStringBuilder = new StringBuilder();

    /** Default constructor that does not enable the {@link System#out output. */
    public StringLogger() {
        mSysOut = false;
    }

    /** Secondary constructor that enables the {@link System#out output. */
    public StringLogger(boolean useSysOut) {
        mSysOut = useSysOut;
    }

    @Override
    public void d(@NonNull String tag, @NonNull String message) {
        String s = tag + ": " + message;
        mStringBuilder.append(s).append('\n');
        if (mSysOut) {
            System.out.println(s);
        }
    }

    @Override
    public void d(@NonNull String tag, @NonNull String message, @Null Throwable tr) {
        String s = tag + ": " + message + ": " + tr;
        mStringBuilder.append(s).append('\n');
        if (mSysOut) {
            System.out.println(s);
        }
    }

    public String getString() {
        return mStringBuilder.toString();
    }

    public void clear() {
        mStringBuilder.setLength(0);
    }
}
