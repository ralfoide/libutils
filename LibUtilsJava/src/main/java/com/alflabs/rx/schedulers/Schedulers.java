package com.alflabs.rx.schedulers;

import com.alflabs.annotations.NonNull;
import com.alflabs.rx.IScheduler;

public class Schedulers {
    private static Sync sSync;
    private static Worker sWorker;

    @NonNull
    public static IScheduler sync() {
        if (sSync == null) {
            sSync = new Sync();
        }
        return sSync;
    }

    @NonNull
    public static IScheduler io() {
        if (sWorker == null) {
            sWorker = new Worker();
        }
        return sWorker;
    }

}
