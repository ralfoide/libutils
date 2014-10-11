/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.app;

import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Context;

/**
 * Wrapper around the {@link BackupManager} API, only available with
 * Froyo (Android API level 8).
 * <p/>
 * This class should not be used directly. Instead, use {@link BackupWrapper}
 * which will delegate calls to this one if it can be loaded (that is if the
 * backup API is available.)
 */
@TargetApi(8)
/* package */ class BaseBackupWrapperImpl {

    private BackupManager mManager;

    public BaseBackupWrapperImpl(Context context) {
        mManager = new BackupManager(context);
    }

    public void dataChanged() {
        if (mManager != null) {
            mManager.dataChanged();
        }
    }

    // TODO: public abstract String getTAG_AppBackupAgent() { return ClockBackupAgent.TAG; }

}
