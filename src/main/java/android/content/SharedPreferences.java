package android.content;

/**
 * Placeholder
 */
public class SharedPreferences {

    public Editor edit() {
        return new Editor();
    }

    public boolean getBoolean(String name, boolean value) {
        return false;
    }

    public int getInt(String name, int value) {
        return value;
    }

    public String getString(String name, String value) {
        return value;
    }

    public class Editor {

        public boolean commit() {
            return false;
        }

        public Editor putBoolean(String name, boolean value) {
            return this;
        }

        public Editor putInt(String name, int value) {
            return this;
        }

        public Editor putString(String name, String value) {
            return this;
        }
    }

}
