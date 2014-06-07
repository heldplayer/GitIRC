
package me.heldplayer.irc.api.plugin;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.heldplayer.irc.api.IClassLoader;
import me.heldplayer.util.json.JSONObject;

public class PluginLoader implements IClassLoader {

    static final Logger log = Logger.getLogger("API");

    private HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();

    private HashMap<String, PluginClassLoader> pluginLoaders = new HashMap<String, PluginClassLoader>();
    private HashSet<Plugin> plugins = new HashSet<Plugin>();

    private HashSet<LibraryClassLoader> libraryLoaders = new HashSet<LibraryClassLoader>();

    private ArrayList<String> unloadedClasses = new ArrayList<String>();

    public Set<Plugin> getAllPlugins() {
        return Collections.unmodifiableSet(this.plugins);
    }

    public int loadPlugins() {
        int count = 0;
        File plugins = new File("./plugins");

        if (plugins.exists() && plugins.isDirectory()) {
            File[] files = plugins.listFiles(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    return file.isFile();
                }

            });

            for (int i = 0; i < files.length; i++) {
                File file = files[i];

                try {
                    this.loadPlugin(file);
                    count++;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Check for a plugin in the workspace if running in eclipse
        InputStream in = PluginLoader.class.getResourceAsStream("/plugin.cfg");

        if (in != null) {
            JSONObject object = new JSONObject(in);
            PluginInfo info = new PluginInfo(object);

            PluginClassLoader loader = null;
            try {
                loader = new PluginClassLoader(this, null, info, this.getClass().getClassLoader());
                this.plugins.add(loader.plugin);
                count++;
            }
            catch (Throwable e) {
                throw new PluginException(String.format("Failed loading plugin in '%s'", (Object) null), e);
            }

            this.pluginLoaders.put(info.name, loader);
        }

        for (Plugin plugin : this.plugins) {
            this.enablePlugin(plugin);
        }

        return count;
    }

    public int loadLibraries() {
        int count = 0;
        File libraries = new File("./libraries");

        if (libraries.exists() && libraries.isDirectory()) {
            File[] files = libraries.listFiles(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    return file.isFile();
                }

            });

            for (int i = 0; i < files.length; i++) {
                File file = files[i];

                try {
                    this.loadLibrary(file);
                    count++;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return count;
    }

    public int unloadPlugins() {
        ArrayList<Plugin> plugins = new ArrayList<Plugin>(this.plugins);

        for (Plugin plugin : plugins) {
            this.disablePlugin(plugin);
        }

        this.plugins.clear();
        this.pluginLoaders.clear();

        return plugins.size();
    }

    public int unloadLibraries() {
        int size = this.libraryLoaders.size();

        for (LibraryClassLoader loader : this.libraryLoaders) {
            Set<String> classes = loader.getClasses();

            for (String clazz : classes) {
                this.removeClass(clazz);
            }
        }

        this.libraryLoaders.clear();

        return size;
    }

    public Class<?> findClass(String name) {
        Class<?> result = this.classes.get(name);

        if (result != null) {
            return result;
        }
        else {
            for (String current : this.pluginLoaders.keySet()) {
                PluginClassLoader loader = this.pluginLoaders.get(current);
                try {
                    result = loader.findClass(name, false);
                }
                catch (ClassNotFoundException e) {}

                if (result != null) {
                    return result;
                }
            }

            for (LibraryClassLoader loader : this.libraryLoaders) {
                try {
                    result = loader.findClass(name, false);
                }
                catch (ClassNotFoundException e) {}

                if (result != null) {
                    return result;
                }
            }
        }

        // Ok, you're desperate
        try {
            result = this.getClass().getClassLoader().loadClass(name);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    void setClass(String name, Class<?> clazz) {
        if (!this.classes.containsKey(name)) {
            this.classes.put(name, clazz);
        }
    }

    void removeClass(String name) {
        if (this.classes.containsKey(name)) {
            this.unloadedClasses.add(name);
        }
        this.classes.remove(name);
    }

    public int getLoadedClassesCount() {
        return this.classes.size();
    }

    public int getUnloadingClassesCount() {
        int count = 0;
        Iterator<String> i = this.unloadedClasses.iterator();
        while (i.hasNext()) {
            String name = i.next();
            try {
                Class<?> clazz = Class.forName(name, false, null);
                if (clazz != null) {
                    count++;
                    continue;
                }
            }
            catch (Throwable e) {
                i.remove();
            }
        }
        return count;
    }

    public PluginInfo getPluginInfo(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("file");
        }

        JarFile jar = null;
        try {
            jar = new JarFile(file);

            JarEntry info = jar.getJarEntry("plugin.cfg");

            if (info == null) {
                return null;
            }

            JSONObject object = new JSONObject(jar.getInputStream(info));
            return new PluginInfo(object);
        }
        catch (IOException e) {
            throw new PluginException(String.format("Failed reading plugin information in '%s'", file), e);
        }
        finally {
            if (jar != null) {
                try {
                    jar.close();
                }
                catch (IOException e) {}
            }
        }
    }

    public Plugin loadPlugin(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("file");
        }

        PluginInfo info = this.getPluginInfo(file);

        if (info == null) {
            PluginLoader.log.info(String.format("Plugin candidate '%s' did not have a plugin.cfg", file));
        }

        PluginClassLoader loader = null;
        try {
            loader = new PluginClassLoader(this, file, info, this.getClass().getClassLoader());
            this.plugins.add(loader.plugin);
        }
        catch (Throwable e) {
            throw new PluginException(String.format("Failed loading plugin in '%s'", file), e);
        }

        this.pluginLoaders.put(info.name, loader);

        return loader.plugin;
    }

    public void enablePlugin(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin");
        }

        if (!plugin.isEnabled()) {
            plugin.logger.info(String.format("Enabling plugin %s", plugin.getInfo().name));

            if (!this.pluginLoaders.containsKey(plugin.getInfo().name)) {
                this.pluginLoaders.put(plugin.getInfo().name, plugin.loader);
            }

            try {
                plugin.setEnabled(true);
            }
            catch (Throwable e) {
                plugin.logger.log(Level.SEVERE, String.format("Error while enabling plugin %s", plugin.getInfo().name), e);
            }

            plugin.logger.info(String.format("Enabled plugin %s", plugin.getInfo().name));
        }
    }

    public void disablePlugin(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin");
        }

        if (plugin.isEnabled()) {
            plugin.logger.info(String.format("Disabling plugin %s", plugin.getInfo().name));

            try {
                plugin.setEnabled(false);
            }
            catch (Throwable e) {
                plugin.logger.log(Level.SEVERE, String.format("Error while disabling plugin %s", plugin.getInfo().name), e);
            }

            this.pluginLoaders.remove(plugin.getInfo().name);

            Set<String> classes = plugin.loader.getClasses();

            for (String clazz : classes) {
                this.removeClass(clazz);
            }

            plugin.logger.info(String.format("Disabled plugin %s", plugin.getInfo().name));
        }
    }

    public void loadLibrary(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("file");
        }

        LibraryClassLoader loader = null;
        try {
            loader = new LibraryClassLoader(this, file, this.getClass().getClassLoader());
        }
        catch (Throwable e) {
            throw new PluginException(String.format("Failed loading plugin in '%s'", file), e);
        }

        this.libraryLoaders.add(loader);
    }

    @Override
    public byte[] findBytes(String name) {
        byte[] result = null;

        for (String current : this.pluginLoaders.keySet()) {
            PluginClassLoader loader = this.pluginLoaders.get(current);

            result = loader.findBytes(name);

            if (result != null) {
                return result;
            }
        }

        for (LibraryClassLoader loader : this.libraryLoaders) {

            result = loader.findBytes(name);

            if (result != null) {
                return result;
            }
        }
        return null;
    }

}
