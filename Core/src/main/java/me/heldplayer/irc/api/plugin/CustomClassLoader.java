
package me.heldplayer.irc.api.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Set;

import me.heldplayer.irc.api.IClassLoader;
import sun.misc.IOUtils;

public abstract class CustomClassLoader extends URLClassLoader implements IClassLoader {

    private PluginLoader loader;
    private String name;

    private HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();

    public CustomClassLoader(PluginLoader loader, File file, ClassLoader parent, String name) throws MalformedURLException {
        super(file == null ? new URL[] {} : new URL[] { file.toURI().toURL() }, parent);

        this.loader = loader;
        this.name = name;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return this.findClass(name, true);
    }

    protected Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result == null) {
            if (checkGlobal) {
                result = loader.findClass(name);
            }

            if (result == null) {
                result = super.findClass(name);

                if (result != null) {
                    loader.setClass(name, result);
                }
            }

            PluginLoader.log.info(String.format("[%s] Loaded class '%s'", this.name, name));

            classes.put(name, result);
        }

        return result;
    }

    Set<String> getClasses() {
        return this.classes.keySet();
    }

    @Override
    public byte[] findBytes(final String name) {
        String str = name.replace('.', '/').concat(".class");
        ClassLoader loader = this.getClass().getClassLoader();
        try {
            byte[] data = IOUtils.readFully(loader.getResourceAsStream(str), -1, true);
            PluginLoader.log.info(String.format("[%s] Found class bytes for '%s'", this.name, name));
            return data;
        }
        catch (IOException e) {
            return null;
        }
    }

}
