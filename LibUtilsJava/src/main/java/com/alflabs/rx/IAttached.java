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
 * indicating the object wants to be notified when the object is attached (added) or detached (removed)
 * from a stream.
 */
public interface IAttached<Event> {
    /**
     * The {@link ISubscriber}, {@link IGenerator}, or {@link IProcessor} was attached (added) to the stream.
     *
     * @throws PublisherAttachedException if publisher is already attached.
     */
    void onAttached(@NonNull IStream<? super Event> stream);

    /**
     * The {@link ISubscriber}, {@link IGenerator}, or {@link IProcessor} was detached (removed) from the stream.
     */
    void onDetached(@NonNull IStream<? super Event> stream);
}
