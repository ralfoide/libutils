package android.content;

import android.annotation.TargetApi;

/**
 * Placeholder
 */
public interface SharedPreferences {

    public Editor edit();

    public boolean getBoolean(String name, boolean value);

    public int getInt(String name, int value);

    public String getString(String name, String value);

    public interface Editor {

        @TargetApi(9)
        public void apply();

        @TargetApi(1)
        public boolean commit();

        public Editor putBoolean(String name, boolean value);

        public Editor putInt(String name, int value);

        public Editor putString(String name, String value);
    }
}
