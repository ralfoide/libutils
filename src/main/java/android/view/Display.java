package android.view;

import android.graphics.Point;

public abstract class Display {
    public abstract void getSize(Point outSize);

    public abstract int getWidth();

    public abstract int getHeight();
}
