package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

public class Schedulers {
    private static _Sync sSync;
    private static _Worker sWorker;

    /** Scheduler that makes direct immediate calls to execute operations. */
    @NonNull
    public static IScheduler sync() {
        if (sSync == null) {
            sSync = new _Sync();
        }
        return sSync;
    }

    /** Scheduler that uses a pool of cached worker threads to execute operations. */
    @NonNull
    public static IScheduler io() {
        if (sWorker == null) {
            sWorker = new _Worker();
        }
        return sWorker;
    }

}
