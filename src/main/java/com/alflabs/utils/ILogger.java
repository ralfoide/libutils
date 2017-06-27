package com.alflabs.utils;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

public interface ILogger {
    void d(@NonNull String tag, @NonNull String message);
    void d(@NonNull String tag, @NonNull String message, @Null Throwable tr);
}
