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

/** Test for {@link StringLogger}. */
public class StringLoggerTest {

    @Test
    public void testStringLogger() throws Exception {
        StringLogger log = new StringLogger();
        assertThat(log.getString()).isEmpty();

        log.d("TAG", "message");
        assertThat(log.getString()).isEqualTo(
                "TAG: message\n");

        log.d("TAG", "message 2", new RuntimeException("Something went wrong"));
        assertThat(log.getString()).isEqualTo(
                "TAG: message\n" +
                "TAG: message 2: java.lang.RuntimeException: Something went wrong\n");

        log.clear();
        assertThat(log.getString()).isEmpty();

        log.d("TAG", "message 3");
        assertThat(log.getString()).isEqualTo(
                "TAG: message 3\n");
    }
}
