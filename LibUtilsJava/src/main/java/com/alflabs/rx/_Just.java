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

import com.alflabs.annotations.NonNull;

/**
 * A simple publisher that publishes all the constructor values when first attached to a stream.
 */
class _Just<E> extends BaseGenerator<E> {
    private final E[] mValues;

    @SafeVarargs
    public _Just(E...values) {
        mValues = values;
    }

    @Override
    public void onAttached(@NonNull IStream<? super E> stream) {
        super.onAttached(stream);
        for (E value : mValues) {
            stream._publishOnStream(value);
        }
    }

    @Override
    public void onDetached(@NonNull IStream<? super E> stream) {}
}
