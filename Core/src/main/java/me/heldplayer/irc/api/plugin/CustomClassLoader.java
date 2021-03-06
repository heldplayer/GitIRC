package me.heldplayer.irc.api.plugin;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IClassLoader;
import me.heldplayer.irc.api.sandbox.SandboxBlacklist;
import sun.misc.IOUtils;
import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Set;

@SandboxBlacklist
public abstract class CustomClassLoader extends URLClassLoader implements IClassLoader {

    private PluginLoader loader;
    private String name;

    private HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();
    private URLClassPath ucp;

    public CustomClassLoader(PluginLoader loader, File file, ClassLoader parent, String name) throws MalformedURLException {
        super(file == null ? new URL[] { } : new URL[] { file.toURI().toURL() }, parent);

        this.loader = loader;
        this.name = name;

        this.ucp = ReflectionHelper.getClassPath(this);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return this.findClass(name, true);
    }

    protected Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
        Class<?> result = this.classes.get(name);

        if (result == null) {
            if (checkGlobal) {
                result = this.loader.findClass(name);
            }

            if (result == null) {
                result = super.findClass(name);

                if (result != null) {
                    this.loader.setClass(name, result);
                }

                if (BotAPI.configuration.getClassLoadingVerbose()) {
                    PluginLoader.log.info(String.format("[%s] Loaded class '%s'", this.name, name));
                }
            }

            this.classes.put(name, result);
        }

        return result;
    }

    Set<String> getClasses() {
        return this.classes.keySet();
    }

    @Override
    public byte[] findBytes(final String name) {
        if (BotAPI.configuration.getClassLoadingVerbose()) {
            PluginLoader.log.info(String.format("[%s] Looking for class bytes for '%s'", this.name, name));
        }
        String str = name.replace('.', '/').concat(".class");
        ClassLoader loader = this.getClass().getClassLoader();
        try {
            byte[] data = IOUtils.readFully(loader.getResourceAsStream(str), -1, true);
            if (BotAPI.configuration.getClassLoadingVerbose()) {
                PluginLoader.log.info(String.format("[%s] Found class bytes for '%s'", this.name, name));
            }
            return data;
        } catch (Throwable e) {
        }

        // Fallback
        if (BotAPI.configuration.getClassLoadingVerbose()) {
            PluginLoader.log.info(String.format("[%s] Looking for class bytes for '%s' in URLClassPath", this.name, name));
        }
        Resource localResource = this.ucp.getResource(str, false);
        if (localResource != null) {
            try {
                byte[] data = localResource.getBytes();
                if (BotAPI.configuration.getClassLoadingVerbose()) {
                    PluginLoader.log.info(String.format("[%s] Found class bytes for '%s'", this.name, name));
                }
                return data;
            } catch (Throwable e) {
            }
        }
        return null;
    }

}
