/*
 * (c) ralfoide gmail com, 2008
 * Project: TimerApp
 * License TBD
 */

package com.alflabs.app;

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


