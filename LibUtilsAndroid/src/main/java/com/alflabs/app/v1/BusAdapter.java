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

public abstract class BusAdapter implements IBusListener {

    @Null
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

    @Null
    public Class<?> getClassFilter() {
        return mClassFilter;
    }

    @Override
    public abstract void onBusMessage(int what, @Null Object object);
}


