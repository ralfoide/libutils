package com.alflabs.kv;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

import java.util.Set;

/**
 * Interface to the key-value part of a {@link KeyValueClient} or {@link KeyValueServer}.
 */
public interface IKeyValue {
    void setOnWriteChangeListener(@Null KeyValueProtocol.OnChangeListener listener);

    /** Returns all the keys available. */
    @NonNull
    Set<String> getKeys();

    /** Returns the value for the given key or null if it doesn't exist. */
    @Null
    String getValue(@NonNull String key);

    /**
     * Sets the non-value for the given key.
     * A null value removes the key if it existed.
     * When broadcast is false, the change is purely internal.
     * When broadcast is true, the server is notified if there's a change.
     */
    void putValue(@NonNull String key, @Null String value, boolean broadcast);
}
