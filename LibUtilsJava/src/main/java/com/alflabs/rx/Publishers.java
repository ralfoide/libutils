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

package com.alflabs.rx;

/**
 * Helper methods and classes for {@link IGenerator}.
 */
public class Publishers {

    /**
     * Returns a default publisher that just sends event into its stream.
     */
    public static <E> IPublisher<E> publisher() {
        return new BasePublisher<>();
    }

    /**
     * Returns a simple publisher that publishes all the given values when first attached to a stream.
     */
    @SafeVarargs
    public static <E> IGenerator<E> just(E...values) {
        return new _Just<E>(values);
    }

    /**
     * Returns a simple publisher that repeats the latest value when a new subscriber is added.
     */
    public static <E> IPublisher<E> latest() {
        return new _Latest<>();
    }
}
