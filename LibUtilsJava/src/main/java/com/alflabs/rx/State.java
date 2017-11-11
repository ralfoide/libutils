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

public enum State {
    /**
     /**
     * Indicates the stream is open yet has no subscribers.
     * It accepts publishers, subscribers and processors.
     * Publishers can publish new events.
     * The stream will NOT deliver events since there are no subscribers.
     * <p/>
     * How the stream behaves when idle is specific to the stream implementation.
     * Typical scenarios are: events are queued then delivered all at once, or delayed or ignored and dropped.
     * It is up to the stream and its compatible publishers/subscribers to define their own contract.
     * <p/>
     * The idle state is really just a detail on the open state. A stream that is paused or closed will
     * report paused or closed no matter have many subscribers are attached to it. When the stream reports
     * idle, it means it is open with no subscribers and is neither closed nor paused.
     * <p/>
     * A idle stream can become open, paused or closed.
     */
    IDLE,
    /**
     * Indicates the stream is open with actual subscribers.
     * It accepts publishers, subscribers and processors.
     * Publishers can publish new events.
     * The stream will deliver events to the subscribers.
     * <p/>
     * An open stream can become paused or closed by clients.
     * It becomes idle when the last subscriber is removed.
     */
    OPEN,
    /**
     * Indicates the stream is open yet paused.
     * It accepts publishers, subscribers and processors.
     * Publishers can publish new events.
     * The stream will NOT deliver events to the subscribers.
     * <p/>
     * How the stream behaves when paused is specific to the stream implementation.
     * Typical scenarios are: events are queued then delivered all at once, or delayed or ignored and dropped.
     * It is up to the stream and its compatible publishers/subscribers to define their own contract.
     * <p/>
     * A paused stream can become open (unpaused) or closed.
     */
    PAUSED,
    /**
     * Indicates the stream if closed.
     * Closing the stream is a terminal action. It cannot be reopened.
     * <p/>
     * Closing a stream is useful when one or more publishers need to signal to all the subscribers that
     * they will stop (for example the app is closing), or when subscribers need to indicate to publishers that
     * they can't receive anymore (for example a network transport was closed).
     */
    CLOSED
}
