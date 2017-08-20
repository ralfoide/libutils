package com.alflabs.rx;

/**
 * Exception thrown by a publisher when trying to attach it to a stream yet it's already attached to another stream.
 */
public class PublisherAttachedException extends RuntimeException {
    static final long serialVersionUID = 1;

    public PublisherAttachedException() {
    }

    public PublisherAttachedException(String message) {
        super(message);
    }

    public PublisherAttachedException(String message, Throwable cause) {
        super(message, cause);
    }
}
