package com.alflabs.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

@SuppressWarnings("WeakerAccess")
@TargetApi(Build.VERSION_CODES.KITKAT)
public class InjectionValidator {

    @SuppressLint("NewApi")
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
