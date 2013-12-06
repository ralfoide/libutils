/*
 * Project: Set Sample
 * Copyright (C) 2013 alf.labs gmail com,
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

package android.util;

/** Fake log implementation just to build the library, not used at runtime. */
public final class Log {
    public static int d(String tag, String msg) { return 0; /* compiled code */ }
    public static int d(String tag, String msg, Throwable tr) { return 0; /* compiled code */ }

    public static int i(String tag, String msg) { return 0; /* compiled code */ }
    public static int i(String tag, String msg, Throwable tr) { return 0; /* compiled code */ }

    public static int w(String tag, String msg) { return 0; /* compiled code */ }
    public static int w(String tag, String msg, Throwable tr) { return 0; /* compiled code */ }
    public static int w(String tag, Throwable tr) { return 0; /* compiled code */ }

    public static int e(String tag, String msg) { return 0; /* compiled code */ }
    public static int e(String tag, String msg, Throwable tr) { return 0; /* compiled code */ }
}
