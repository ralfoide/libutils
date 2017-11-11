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
import com.alflabs.annotations.Null;

/**
 * A default publisher that just sends events into its stream.
 * <p/>
 * A publisher can only be attached to a single stream at once.
 * Before attaching to a new stream, it must be detached from the previous stream.
 * <p/>
 * This is a perfect base class for custom publishers.
 */
public class BasePublisher<E> extends BaseGenerator<E> implements IPublisher<E> {

    @NonNull
    public IPublisher<E> publish(@Null E event) {
        super.publishOnStream(event);
        return this;
    }
}
