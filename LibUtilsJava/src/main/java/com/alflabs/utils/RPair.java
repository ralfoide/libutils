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

@SuppressWarnings("WeakerAccess")
public class RPair<A, B> {
    public final A first;
    public final B second;

    protected RPair(A a, B b) {
        first = a;
        second = b;
    }

    public static <A, B> RPair<A, B> create(A a, B b) {
        return new RPair<>(a, b);
    }

    @Override
    public String toString() {
        return "{" + first + ", " + second + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }

        RPair<?, ?> rhs = (RPair<?, ?>) o;

        return (first != null ? first.equals(rhs.first) : rhs.first == null)
                && (second != null ? second.equals(rhs.second) : rhs.second == null);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}
