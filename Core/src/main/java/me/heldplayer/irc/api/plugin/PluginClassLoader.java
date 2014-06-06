
package me.heldplayer.irc.api.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import me.heldplayer.irc.api.IClassLoader;
import sun.misc.IOUtils;

public class PluginClassLoader extends URLClassLoader implements IClassLoader {

    private PluginLoader loader;
    private PluginInfo info;
    Plugin plugin;

    private HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();

    public PluginClassLoader(PluginLoader loader, File file, PluginInfo info, ClassLoader parent) throws MalformedURLException {
        super(file == null ? new URL[] {} : new URL[] { file.toURI().toURL() }, parent);

        this.loader = loader;
        this.info = info;

        try {
            Class<?> clazz;
            try {
                clazz = Class.forName(info.mainClass, true, this);
            }
            catch (ClassNotFoundException e) {
                throw new PluginException(String.format("Could not find main class '%s'", info.mainClass), e);
            }

            Class<? extends Plugin> plugin;
            try {
                plugin = clazz.asSubclass(Plugin.class);
            }
            catch (ClassCastException e) {
                throw new PluginException(String.format("Main class '%s' does not extend Plugin", info.mainClass), e);
            }

            this.plugin = plugin.newInstance();

            this.plugin.info = info;
            this.plugin.loader = this;
            this.plugin.logger = Logger.getLogger(info.name);
        }
        catch (InstantiationException e) {
            throw new PluginException(String.format("Could not create instance of '%s'", info.mainClass), e);
        }
        catch (IllegalAccessException e) {
            throw new PluginException(String.format("Main class '%s' does not have a public constructor", info.mainClass), e);
        }
    }

    public PluginInfo getInfo() {
        return this.info;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return this.findClass(name, true);
    }

    Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
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

            PluginLoader.log.info(String.format("[%s] Loaded class '%s'", this.info.name, name));

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
            return IOUtils.readFully(loader.getResourceAsStream(str), -1, true);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
