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

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

@SuppressWarnings("WeakerAccess")
public class InjectionValidator {

    public static void check(final Object instance) {
        assertThat(instance).isNotNull();

        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.getDeclaredAnnotation(Inject.class) != null)
                    .forEach(field -> assertThat(getTarget(instance, field)).named(field.getName()).isNotNull());
        }
    }

    private static Object getTarget(Object instance, Field field) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new AssertionError("InjectionValidator failed to get value for field " + field.getName(), e);
        }
    }
}
