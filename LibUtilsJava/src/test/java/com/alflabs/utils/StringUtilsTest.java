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

public class StringUtilsTest {

    @Test
    public void testCapitalize() throws Exception {
        assertThat(StringUtils.capitalize(null)).isEmpty();
        assertThat(StringUtils.capitalize("")).isEmpty();

        assertThat(StringUtils.capitalize("  ")).isEqualTo("  ");
        assertThat(StringUtils.capitalize("123")).isEqualTo("123");
        assertThat(StringUtils.capitalize("a")).isEqualTo("A");
        assertThat(StringUtils.capitalize("ab")).isEqualTo("Ab");
        assertThat(StringUtils.capitalize("AB")).isEqualTo("Ab");
        assertThat(StringUtils.capitalize("Hello World")).isEqualTo("Hello world");
    }
}
