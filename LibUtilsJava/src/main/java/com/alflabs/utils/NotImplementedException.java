package com.alflabs.utils;

/**
 * Indicates that something is not implemented -- either "yet" or "ever" or "on-demand".
 * <p/>
 * UseD when implementing interfaces where a particular method is not useful
 * and it is not expected this would ever be called, for example when stubbing during tests.
 * <p/>
 * This however is a code smell if used in a "this should happen" scenario.
 */
public class NotImplementedException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public NotImplementedException() {
    }

    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public NotImplementedException(Throwable throwable) {
        super(throwable);
    }
}
