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

import java.util.concurrent.atomic.AtomicReference;

/**
 * A base class for custom publishers/generators that tracks the current attached stream.
 */
public class BaseGenerator<E> implements IGenerator<E>, IAttached<E>, IStateChanged<E> {

    private AtomicReference<IStream<? super E>> mStream = new AtomicReference<>();

    @Null
    public IStream<? super E> getStream() {
        return mStream.get();
    }

    protected void publishOnStream(@Null E event) {
        IStream<? super E> stream = getStream();
        if (stream != null) {
            stream._publishOnStream(event);
        }
    }

    @Override
    public void onAttached(@NonNull IStream<? super E> stream) {
        if (mStream.get() != null) {
            throw new PublisherAttachedException("Publisher is already attached to a stream.");
        }
        mStream.set(stream);
    }

    @Override
    public void onDetached(@NonNull IStream<? super E> stream) {
        mStream.set(null);
    }

    @Override
    public void onStateChanged(@NonNull IStream<? super E> stream, @NonNull State newState) {
        if (newState == State.CLOSED && stream == mStream.get()) {
            mStream.lazySet(null); // for GC
        }
    }
}
