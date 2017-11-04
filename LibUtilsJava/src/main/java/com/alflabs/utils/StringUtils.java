package com.alflabs.utils;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

public class StringUtils {

    @NonNull
    public static String capitalize(@Null String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

}
