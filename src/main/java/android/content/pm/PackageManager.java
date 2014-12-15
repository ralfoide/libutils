package android.content.pm;

import android.util.AndroidException;

/**
 * Placeholder
 */
public abstract class PackageManager {

    /**
     * This exception is thrown when a given package, application, or component
     * name cannot be found.
     */
    public static class NameNotFoundException extends AndroidException {
        public NameNotFoundException() {
        }

        public NameNotFoundException(String name) {
            super(name);
        }
    }

    /**
     * {@link PackageInfo} flag: return information about the
     * signatures included in the package.
     */
    public static final int GET_SIGNATURES          = 0x00000040;

    public abstract PackageInfo getPackageInfo(String packageName, int flags)
            throws NameNotFoundException;
}
