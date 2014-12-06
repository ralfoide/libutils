/*
 * Project: AndroidAppLib
 * Copyright (C) 2014 ralfoide gmail com.
 */

package com.alflabs.app;

/**
 * Holder for app-wide encoded strings.
 */
public interface ICoreStrings {
    public static final int GOOGLE_ANALYTICS_KEY = 0;
    public static final int DEBUG_KEY1 = 1;
    public static final int DEBUG_KEY2 = 2;

    String get(int code);
}
