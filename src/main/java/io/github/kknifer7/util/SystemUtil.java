package io.github.kknifer7.util;

/**
 * @author Knifer
 */
public final class SystemUtil {

    public static final boolean IS_OS_WINDOWS;
    public static final boolean IS_OS_MAC;
    public static final boolean IS_OS_LINUX;

    private SystemUtil() {
        throw new AssertionError();
    }

    static {
        String osName = System.getProperty("os.name");

        if (osName == null) {
            IS_OS_WINDOWS = false;
            IS_OS_MAC = false;
            IS_OS_LINUX = false;
        } else {
            IS_OS_WINDOWS = osName.startsWith("Windows");
            IS_OS_MAC = osName.startsWith("Mac");
            IS_OS_LINUX = osName.startsWith("Linux");
        }
    }
}
