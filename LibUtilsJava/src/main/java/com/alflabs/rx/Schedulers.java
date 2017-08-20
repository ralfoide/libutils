package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

public class Schedulers {
    private static _Sync sSync;
    private static _Worker sWorker;

    @NonNull
    public static IScheduler sync() {
        if (sSync == null) {
            sSync = new _Sync();
        }
        return sSync;
    }

    @NonNull
    public static IScheduler io() {
        if (sWorker == null) {
            sWorker = new _Worker();
        }
        return sWorker;
    }

}
