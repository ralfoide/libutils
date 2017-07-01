package com.alflabs.kv;

public interface IConnection {
    /** Starts the server connection. Returns immediately. */
    public void startAsync();
    /** Stops the server connection and waits till the communication thread ends. */
    public void stopSync();
    /** Stops the server connection. Returns immediately. */
    public void stopAsync();
}
