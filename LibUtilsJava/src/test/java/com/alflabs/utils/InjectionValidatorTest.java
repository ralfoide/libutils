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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class InjectionValidatorTest {
    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSuccess() throws Exception {
        InjectionValidator.check(new AllInjectedSub());
    }

    @Test
    public void testFailure() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Not true that \"field2\" is a non-null reference");
        InjectionValidator.check(new NotAllInjectedSub());
    }

    public static class AllInjectedBase {
        @Inject Object field1;
        @Inject Double field2;

        public AllInjectedBase() {
            this.field1 = "Foo";
            this.field2 = 42.0;
        }
    }

    public static class AllInjectedSub extends AllInjectedBase {
        @Inject String field3;

        public AllInjectedSub() {
            this.field3 = "Bar";
        }
    }

    public static class NotAllInjectedBase {
        @Inject Object field1;
        @Inject Double field2;

        public NotAllInjectedBase() {
            this.field1 = "Foo";
            // this.field2 is not set
        }
    }

    public static class NotAllInjectedSub extends NotAllInjectedBase {
        @Inject String field3;

        public NotAllInjectedSub() {
            this.field3 = "Bar";
        }
    }

}
