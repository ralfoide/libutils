package android.view;

import android.graphics.drawable.Drawable;

public abstract class View {
    public abstract void setSystemUiVisibility(int visibility);

    public abstract void setAlpha(float alpha);

    public static int generateViewId() { return 0; }

    public abstract void setBackgroundDrawable(Drawable background);

    public abstract void setBackground(Drawable background);
}
