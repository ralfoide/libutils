package android.os;

import android.annotation.TargetApi;

public abstract class Looper {
    @TargetApi(1)
    public abstract void quit();
    @TargetApi(18)
    public abstract void quitSafely();
}
