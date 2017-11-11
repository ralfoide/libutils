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

package java.util.function;

import android.annotation.SuppressLint;

/**
 * DUMMY VERSION for Java 1.8 compile-time compatibility for InjectionValidator.java
 * <p/>
 * FIXME: Remove once can use proper Java 1.8 without Jack compiler in Android Library (with AS 3.0+)
 * <p/>
 * Represents a predicate (boolean-valued function) of one argument.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #test(Object)}.
 */
@FunctionalInterface
@SuppressLint("NewApi")
public interface Predicate<T> {
    /**
     * Tests this operation on the given argument.
     */
    boolean test(T value);
}
