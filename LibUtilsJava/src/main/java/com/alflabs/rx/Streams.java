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
 * Helper methods and classes for {@link IStream}.
 */
public class Streams {

    /**
     * Creates a new {@link IStream} with no subscribers and no publishers.
     * <p/>
     * The stream is thread-safe. Adding or removing publishers and schedulers, as well as invoking
     * {@link IStream#publish} can be done from any thread. The stream implements a basic queue -- when paused,
     * all published events are queued and delivered when the stream is reopen. Similarly, any events queued
     * before the first publisher are delivered once the first subscriber or processor is attached.
     * <p/>
     * Publish calls are protected by a lock and events are guaranteed to be delivered in a FIFO order.
     * Subscribers are never called from within the publish lock and a subscriber can thus safely publish
     * onto the same stream than it receives.
     * <p/>
     * Changing the stream state is immediate. Consequently, calling publish on the io scheduler followed by
     * a pause/close call may result in the published event happen in any order with regard to the state change.
     * <p/>
     * The stream uses {@link Schedulers#io()} by default unless changed by {@link IStream#on(IScheduler)}.
     */
    @NonNull
    public static <Event> IStream<Event> stream() {
        return new _Stream<>(Schedulers.io());
    }
}
