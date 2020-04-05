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

package com.alflabs.app.v1;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class BusesTest {

    @Test
    public void testNewBus() {
        Bus b0 = Buses.newBus();
        assertThat(b0).isNotNull();
        assertThat(b0.getId()).isGreaterThan(0);
        assertThat(b0).isSameInstanceAs(Buses.getById(b0.getId()));

        Bus b1 = Buses.newBus();
        assertThat(b1).isNotNull();
        assertThat(b1.getId()).isEqualTo(b0.getId() + 1);
        assertThat(b1).isSameInstanceAs(Buses.getById(b1.getId()));
    }
}
