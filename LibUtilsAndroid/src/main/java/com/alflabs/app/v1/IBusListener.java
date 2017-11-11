/*
 * Project: Lib Utils
 * Copyright (C) 2008 alf.labs gmail com,
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

package com.alflabs.app.v1;

import com.alflabs.annotations.Null;


//-----------------------------------------------

/**
 * Listen to {@link Bus} notifications.
 */
public interface IBusListener {

    /**
     * Callback invoked when a message matching the {@link BusAdapter} class filter
     * is received.
     * <p/>
     * Users must be aware that there is no thread specification -- the callback is
     * invoked on the sender's thread, which may or may not be the UI thread.
     *
     * @param what The integer "what" portion of the message. {@link Bus#NO_WHAT} if not specified.
     * @param object A potentially null message object.
     */
    public abstract void onBusMessage(int what, @Null Object object);
}


