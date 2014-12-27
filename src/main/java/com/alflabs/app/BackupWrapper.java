/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.app;

import android.app.backup.BackupManager;
import android.content.Context;
import android.util.Log;

/**
 * Wrapper around the {@link BackupManager} API, which is only available
 * starting with Froyo (Android API 8).
 *
 * The actual work is done in the class BackupWrapperImpl, which uses the
 * API methods, and thus will fail to load with a VerifyError exception
 * on older Android versions. The wrapper defers to the impl class if
 * it loaded, and otherwise just drops all the calls.
 */
public class BackupWrapper {

    public static final String TAG = BackupWrapper.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final Object[] sLock = new Object[0];

    private final BaseBackupWrapperImpl mImpl;

    public BackupWrapper(Context context) {
    	BaseBackupWrapperImpl b = null;
        try {
            // Try to load the actual implementation. This may fail.
            b = new BaseBackupWrapperImpl(context);
        } catch (Throwable e) {
            // No need to log an error, this is expected if API < 8.
            // This is not expected if API >= 8
            if (DEBUG) Log.w(TAG, "Failed to load: " + e.toString());
        }
        mImpl = b;
    }

    public void dataChanged() {
        if (mImpl != null) {
            mImpl.dataChanged();
            if (DEBUG) Log.d(TAG, "Backup dataChanged");
        }
    }

// TODO
//    /**
//     * Returns the TAG for ClockBackupAgent if it loaded, or null.
//     */
//    public String getTAG_AppBackupAgent() {
//        if (mImpl != null) {
//            mImpl.getTAG_AppBackupAgent();
//        }
//        return null;
//    }

    /**
     * This lock must be used by all parties that want to manipulate
     * directly the files being backup/restored. This ensures that the
     * backup agent isn't trying to backup or restore whilst the other
     * party is modifying them directly.
     * <p/>
     * In our case, this MUST be used by the save/restore from SD operations.
     * <p/>
     * Implementation detail: since {@code ClockBackupAgent} depends
     * on the BackupAgent class, it is not available on platform < API 8.
     * This means any direct access to this class must be avoided.
     */
    public static Object getBackupLock() {
        return sLock;
    }
}
