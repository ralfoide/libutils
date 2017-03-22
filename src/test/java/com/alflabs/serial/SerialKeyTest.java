/*
 * Project: Set Sample
 * Copyright (C) 2013 alf.labs gmail com,
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


package com.alflabs.serial;

import static com.google.common.truth.Truth.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SerialKeyTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testEncodeNewKey() throws Exception {
        SerialKey s = new SerialKey();
        assertThat(s.encodeNewKey("foo" )).isEqualTo("foo" .hashCode());
        assertThat(s.encodeNewKey("blah")).isEqualTo("blah".hashCode());

        // "encodeNew" returns the same integer if the same key is used
        assertThat(s.encodeNewKey("foo")).isEqualTo("foo".hashCode());
        assertThat(s.encodeNewKey("foo")).isEqualTo("foo".hashCode());

    }

    @Test
    public void testEncodeUniqueKey() throws Exception {
        SerialKey s = new SerialKey();
        assertThat(s.encodeUniqueKey("foo" )).isEqualTo("foo" .hashCode());
        assertThat(s.encodeUniqueKey("blah")).isEqualTo("blah".hashCode());

        // encodeUnique throws an except if trying to use a key previously used
        exception.expect(SerialKey.DuplicateKey.class);
        exception.expectMessage("Key name collision: 'foo' has the same hash than previously used 'foo'");
        s.encodeUniqueKey("foo");
    }

    @Test
    public void testEncodeKey() throws Exception {
        SerialKey s = new SerialKey();
        assertThat(s.encodeKey("foo" )).isEqualTo("foo" .hashCode());
        assertThat(s.encodeKey("blah")).isEqualTo("blah".hashCode());
    }
}
