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

package com.alflabs.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class RPairTest {

    @Test
    public void testCreate() {
        RPair<String, String> p1 = RPair.create("First", "Second");
        assertThat(p1).isNotNull();
        assertThat(p1.first).isEqualTo("First");
        assertThat(p1.second).isEqualTo("Second");
        assertThat(p1.toString()).isEqualTo("{First, Second}");

        RPair<String, String> p2 = RPair.create("First", "Second");
        assertThat(p1).isEqualTo(p2);
        assertThat(p1).isNotSameInstanceAs(p2);

        RPair<String, String> p3 = RPair.create("First", "Third");
        assertThat(p1).isNotEqualTo(p3);
    }
}
