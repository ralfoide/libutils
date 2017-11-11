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
 * A subscriber adapter that provides default implementations to all the methods from
 * {@link ISubscriber}, including those from the optional {@link IStateChanged} and {@link IAttached} interfaces.
 * <p/>
 * It's probably overkill and has not much application outside of convenience for testing
 * and thus has been moved to the test package.
 */
class _SubAdapter<Event> implements ISubscriber<Event>, IStateChanged<Event>, IAttached<Event> {
    @Override
    public void onStateChanged(@NonNull IStream<? super Event> stream, @NonNull State newState) {}

    @Override
    public void onReceive(@NonNull IStream<? extends Event> stream, @Null Event event) {}

    @Override
    public void onAttached(@NonNull IStream<? super Event> stream) {}

    @Override
    public void onDetached(@NonNull IStream<? super Event> stream) {}
}
