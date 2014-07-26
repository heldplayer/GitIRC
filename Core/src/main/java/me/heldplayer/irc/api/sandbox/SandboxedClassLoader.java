package me.heldplayer.irc.api.sandbox;

import me.heldplayer.irc.api.BotAPI;

import java.lang.annotation.Annotation;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@SandboxBlacklist
public abstract class SandboxedClassLoader extends SecureClassLoader {

    private static final Logger log = Logger.getLogger("Sandbox");
    protected static Set<String> loaderExceptions = new HashSet<String>();
    private HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();

    public SandboxedClassLoader() {
        super(null);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (SandboxedClassLoader.loaderExceptions.contains(name)) {
            if (BotAPI.configuration.getClassLoadingVerbose()) {
                SandboxedClassLoader.log.info(String.format("[%s] Loading class '%s' through main PluginLoader", this.getName(), name));
            }

            return BotAPI.pluginLoader.findClass(name);
        }

        Class<?> result = this.classes.get(name);

        if (result == null) {
            byte[] bytes = BotAPI.pluginLoader.findBytes(name);

            try {
                result = this.defineClass(name, bytes, 0, bytes.length);
            } catch (Throwable e) {
                throw new ClassNotFoundException(name, e);
            }

            if (result != null) {
                Annotation[] annotations = result.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().getCanonicalName().equals(SandboxBlacklist.class.getCanonicalName())) {
                        throw new ClassNotFoundException(String.format("'%s' is blacklisted from being loaded in a sandbox", name));
                    }
                }
            }

            if (BotAPI.configuration.getClassLoadingVerbose()) {
                SandboxedClassLoader.log.info(String.format("[%s] Loaded class '%s'", this.getName(), name));
            }

            this.classes.put(name, result);
        }

        return result;
    }

    public abstract String getName();

    Set<String> getClasses() {
        return this.classes.keySet();
    }

}
