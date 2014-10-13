/*
 * (c) ralfoide gmail com, 2008
 * Project: TimerApp
 * License TBD
 */

package com.alflabs.app;

import com.alflabs.annotations.Null;

//-----------------------------------------------

public abstract class BusAdapter implements IBusListener {

    private final Class<?> mClassFilter;

    /** Creates a bus listener that receives all type of data. */
    public BusAdapter() {
        this(null);
    }

    /**
     * Creates a bus listener that receives only object data of that type.
     *
     * @param classFilter The object class to filter. Can be null to receive everything.
     */
    public BusAdapter(@Null Class<?> classFilter) {
        mClassFilter = classFilter;
    }

    public Class<?> getClassFilter() {
        return mClassFilter;
    }

    @Override
    public abstract void onBusMessage(int what, @Null Object object);
}


