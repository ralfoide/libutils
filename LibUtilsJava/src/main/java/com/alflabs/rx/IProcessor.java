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
 * A processor transform events from an input stream into events for an output stream.
 * <p/>
 * Processors are typically filters, generators, or transformers (aka "map" in the functional terminology).
 * <p/>
 * Processors subscribe to one or more input streams (to receive events) and publish on an output stream.
 * Processors can have more than one input stream (e.g. a merge/combine processor).
 * <p/>
 * Optional interface: <br/>
 * - if the processor implements {@link IStateChanged<InEvent>}, it will be notified when an input stream changes state. <br/>
 * - if the processor implements {@link IAttached<InEvent>}, it will be notified when attached to an input stream. <br/>
 */
public interface IProcessor<InEvent, OutEvent> {
    @Null IStream<OutEvent> output();
    void process(InEvent event);
}
