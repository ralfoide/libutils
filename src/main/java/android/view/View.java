package android.view;

public abstract class View {
    public abstract void setSystemUiVisibility(int visibility);

    public abstract void setAlpha(float alpha);

    public static int generateViewId() { return 0; }
}
