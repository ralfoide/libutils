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
 * A simple publisher that repeats the latest value when a new subscriber is added.
 * <p/>
 * This particular publisher does not publish null events.
 */
class _Latest<E> extends BasePublisher<E> implements ISubscriberAttached<E> {

    private E mLastEvent;

    @NonNull
    public IPublisher<E> publish(@Null E event) {
        mLastEvent = event;
        if (event != null) {
            IStream<? super E> stream = getStream();
            if (stream != null && stream.isOpen()) {
                super.publish(event);
            }
        }
        return this;
    }

    @Override
    public void onSubscriberAttached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {
        if (mLastEvent != null) {
            super.publish(mLastEvent);
        }
    }

    @Override
    public void onSubscriberDetached(@NonNull IStream<? super E> stream, @NonNull ISubscriber<? super E> subscriber) {}
}
