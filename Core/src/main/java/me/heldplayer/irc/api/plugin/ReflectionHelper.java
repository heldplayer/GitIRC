
package me.heldplayer.irc.api.plugin;

import java.lang.reflect.Field;
import java.net.URLClassLoader;

import sun.misc.URLClassPath;

public final class ReflectionHelper {

    private ReflectionHelper() {}

    private static Field ucpField;

    static {
        Class<URLClassLoader> classURLClassLoader = URLClassLoader.class;
        try {
            ucpField = classURLClassLoader.getDeclaredField("ucp");
            ucpField.setAccessible(true);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static URLClassPath getClassPath(URLClassLoader loader) {
        try {
            return (URLClassPath) ucpField.get(loader);
        }
        catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

}
