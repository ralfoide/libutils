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

import com.google.common.truth.Expect;
import static com.google.common.truth.Truth.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.Serializable;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 16)
public class UtilsTest {
    @Rule public final Expect expect = Expect.create();

    @Test
    public void testGetApiLevel() throws Exception {
        assertThat(Utils.getApiLevel()).isGreaterThan(0);
    }

    @Test
    public void testCheckMinApiLevel() throws Exception {
        assertThat(Utils.checkMinApiLevel( 0)).isTrue();
        assertThat(Utils.checkMinApiLevel( 1)).isTrue();
        assertThat(Utils.checkMinApiLevel(42)).isFalse();
    }

    @Test
    public void testIsEmulator() throws Exception {
        // The fake Build from these test can't satisfy the emulator check
        assertThat(Utils.isEmulator()).isFalse();
    }

    @Test
    public void testIsUsingDebugKey() throws Exception {
        // The fake mock overrides from these test can't satisfy the check.
        // It will probably generate an NPE which gets catched and returns false.
        assertThat(Utils.isUsingDebugKey(null, null)).isFalse();
    }

    @Test
    public void testSerializeToString() throws Exception {
        JavaSerializableClass r1 = new JavaSerializableClass(true, 42, 3141592, "some serializable object");

        String s1 = Utils.serializeToString(r1);
        assertThat(s1).isNotNull();
        expect.that(_reformatLongString(s1)).isEqualTo(_reformatLongString(
                "aced000573720031636f6d2e616c666c6162732e7574696c732e5574696c7354657374244a617661" +
                        "53657269616c697a61626c65436c61737300000000000000010200045a00016249000169" +
                        "4a00016c4c0001737400124c6a6176612f6c616e672f537472696e673b7870010000002a" +
                        "00000000002fefd8740018736f6d652073657269616c697a61626c65206f626a656374"));
        assertThat(s1).isEqualTo(
                "aced000573720031636f6d2e616c666c6162732e7574696c732e5574696c7354657374244a617661" +
                        "53657269616c697a61626c65436c61737300000000000000010200045a00016249000169" +
                        "4a00016c4c0001737400124c6a6176612f6c616e672f537472696e673b7870010000002a" +
                        "00000000002fefd8740018736f6d652073657269616c697a61626c65206f626a656374");

        Object r2 = Utils.deserializeFromString(s1);
        assertThat(r2).isNotNull();
        assertThat(r2).isInstanceOf(JavaSerializableClass.class);
        assertThat(r2).isNotSameAs(r1);
        //noinspection ConstantConditions
        ((JavaSerializableClass) r2).isEqualTo(true, 42, 3141592, "some serializable object");
    }

    private String _reformatLongString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = s.length(); i < n; ++i) {
            sb.append(s.charAt(i));
            if ((i & 7) == 7) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Example of Java serializable class, used in the serialization/deserialization test above.
     */
    @SuppressWarnings("WeakerAccess")
    static class JavaSerializableClass implements Serializable {

        private static final long serialVersionUID = 1L;

        public boolean b;
        public int i;
        public long l;
        public String s;

        public JavaSerializableClass(boolean b, int i, long l, String s) {
            this.b = b;
            this.i = i;
            this.l = l;
            this.s = s;
        }

        public void isEqualTo(boolean b, int i, long l, String s) {
            assertThat(this.b).isEqualTo(b);
            assertThat(this.i).isEqualTo(i);
            assertThat(this.l).isEqualTo(l);
            assertThat(this.s).isEqualTo(s);
        }

    }
}
