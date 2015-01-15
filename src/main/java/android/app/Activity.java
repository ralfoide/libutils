package android.app;

import android.annotation.TargetApi;

public abstract class Activity {
    @TargetApi(17)
    public abstract boolean isDestroyed();
}
