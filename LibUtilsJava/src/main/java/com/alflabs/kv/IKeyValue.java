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

package com.alflabs.kv;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.rx.IStream;

import java.util.Set;

/**
 * Interface to the key-value part of a {@link KeyValueClient} or {@link KeyValueServer}.
 */
public interface IKeyValue {
    /**
     *  Retrieves the stream notified when a write command updated a key and its value has changed.
     *  The published string is the name of the key that changed. It is guaranteed to be non-null.
     *  Clients can call {@link #getValue(String)} with that key if they are interested in the new value.
     */
    @NonNull
    IStream<String> getChangedStream();

    /** Returns all the keys available. */
    @NonNull
    Set<String> getKeys();

    /** Returns the value for the given key or null if it doesn't exist. */
    @Null
    String getValue(@NonNull String key);

    /**
     * Sets the non-value for the given key.
     * A null value removes the key if it existed.
     * When broadcast is false, the change is purely internal.
     * When broadcast is true, the server is notified if there's a change.
     */
    void putValue(@NonNull String key, @Null String value, boolean broadcast);
}
