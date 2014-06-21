
package me.heldplayer.irc.api.plugin;

import java.lang.reflect.Field;
import java.net.URLClassLoader;

import me.heldplayer.irc.api.sandbox.SandboxBlacklist;
import sun.misc.URLClassPath;

@SandboxBlacklist
public final class ReflectionHelper {

    private ReflectionHelper() {}

    private static Field ucpField;

    static {
        Class<URLClassLoader> classURLClassLoader = URLClassLoader.class;
        try {
            ReflectionHelper.ucpField = classURLClassLoader.getDeclaredField("ucp");
            ReflectionHelper.ucpField.setAccessible(true);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static URLClassPath getClassPath(URLClassLoader loader) {
        try {
            return (URLClassPath) ReflectionHelper.ucpField.get(loader);
        }
        catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

}
