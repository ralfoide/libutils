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
 * Optional decorator interface for {@link ISubscriber}, {@link IGenerator}, and {@link IProcessor}
 * indicating the object wants to be notified when the stream's {@link State} changes.
 */
public interface IStateChanged<Event> {
    /** Notified when the stream state has changed (paused or closed). */
    void onStateChanged(@NonNull IStream<? super Event> stream, @NonNull State newState);
}
