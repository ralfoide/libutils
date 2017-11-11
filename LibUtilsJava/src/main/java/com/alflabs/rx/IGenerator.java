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

import com.alflabs.annotations.Null;

/**
 * A publisher and a generator both publish events to a stream.
 * The semantical difference is that a publisher allows its callers to publish on the stream
 * whereas a generator does the publishing internally and doesn't allow callers to do the publishing directly.
 * <p/>
 * A publisher is generally a synchronous or asynchronous object that generates one or more event
 * and publishes them directly to the underlying stream when attached to it.
 * <p/>
 * Publishers are attached to a single stream and operate on the scheduler indicated when attached.
 * <p/>
 * Optional interface: <br/>
 * - if the publisher implements {@link IStateChanged<Event>}, it will be notified when the stream changes state. <br/>
 * - if the publisher implements {@link IAttached<Event>}, it will be notified when attached to the stream. <br/>
 * - if the publisher implements {@link ISubscriberAttached<Event>}, it will be notified when a subscriber is attached to the stream. <br/>
 */
public interface IGenerator<Event> {
    @Null
    IStream<? super Event> getStream();
}
