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
 * A Subscriber is a reader, consumer, observer. It takes a stream and is notified when an event is published.
 * <p/>
 * To start receiving, a subscriber needs to be subscribed to a stream.
 * It will be invoked by the stream using the scheduler indicated when subscribing.
 * <p/>
 * The same subscriber can be subscribed to more than one stream, even on different schedulers.
 * <p/>
 * Optional interface: <br/>
 * - if the subscriber implements {@link IStateChanged<Event>}, it will be notified when the stream changes state. <br/>
 * - if the subscriber implements {@link IAttached<Event>}, it will be notified when attached to the stream. <br/>
 */
public interface ISubscriber<Event> {
    /** Receives an event that was published to the stream, if attached to one. */
    void onReceive(@NonNull IStream<? extends Event> stream, @Null Event event);
}
