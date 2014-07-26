package me.heldplayer.irc.api.plugin;

import me.heldplayer.irc.IRCBotLauncher;
import me.heldplayer.irc.api.IClassLoader;
import me.heldplayer.irc.api.plugin.PluginInfo.DependencyOrder;
import me.heldplayer.util.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginLoader implements IClassLoader {

    static final Logger log = Logger.getLogger("API");

    private HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();

    private HashMap<String, PluginClassLoader> pluginLoaders = new HashMap<String, PluginClassLoader>();
    private TreeSet<PluginClassLoader> plugins = new TreeSet<PluginClassLoader>(new Comparator<PluginClassLoader>() {

        @Override
        public int compare(PluginClassLoader arg0, PluginClassLoader arg1) {
            DependencyOrder order0 = arg0.info.getOrder(arg1.info.name);
            DependencyOrder order1 = arg1.info.getOrder(arg0.info.name);

            if (order0 == null && order1 == null) {
                return 1;
            } else if (order0 == null) {
                return -order1.compare;
            } else if (order1 == null) {
                return order0.compare;
            } else {
                if (order0.require && order1.require && order0.compare != -order1.compare) {
                    throw new PluginException("Conflicting entries for dependencies");
                }
                if (order0.require) {
                    return order0.compare;
                }
                if (order1.require) {
                    return order1.compare;
                }
                if (order0.compare != -order1.compare) {
                    throw new PluginException("Conflicting entries for dependencies");
                }
                return order0.compare;
            }
        }

    });

    private HashSet<LibraryClassLoader> libraryLoaders = new HashSet<LibraryClassLoader>();

    private ArrayList<String> unloadedClasses = new ArrayList<String>();

    public Set<PluginClassLoader> getAllPlugins() {
        return Collections.unmodifiableSet(this.plugins);
    }

    public int loadPlugins() {
        int count = 0;
        File plugins = new File(IRCBotLauncher.rootDirectory, "plugins");

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
                } catch (Exception e) {
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
                this.plugins.add(loader);
                count++;
            } catch (Throwable e) {
                throw new PluginException(String.format("Failed loading plugin in '%s'", (Object) null), e);
            }

            this.pluginLoaders.put(info.name, loader);
        }

        for (PluginClassLoader loader : this.plugins) {
            loader.initializePlugin();
            this.enablePlugin(loader);
        }

        return count;
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
            this.plugins.add(loader);
        } catch (Throwable e) {
            throw new PluginException(String.format("Failed loading plugin in '%s'", file), e);
        }

        this.pluginLoaders.put(info.name, loader);

        return loader.plugin;
    }

    public void enablePlugin(PluginClassLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("plugin");
        }

        if (!loader.plugin.isEnabled()) {
            loader.plugin.logger.info(String.format("Enabling plugin %s", loader.getInfo().name));

            if (!this.pluginLoaders.containsKey(loader.getInfo().name)) {
                this.pluginLoaders.put(loader.getInfo().name, loader);
            }

            try {
                loader.plugin.setEnabled(true);
            } catch (Throwable e) {
                loader.plugin.logger.log(Level.SEVERE, String.format("Error while enabling plugin %s", loader.getInfo().name), e);
            }

            loader.plugin.logger.info(String.format("Enabled plugin %s", loader.getInfo().name));
        }
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
        } catch (IOException e) {
            throw new PluginException(String.format("Failed reading plugin information in '%s'", file), e);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public int loadLibraries() {
        int count = 0;
        File libraries = new File(IRCBotLauncher.rootDirectory, "libraries");

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return count;
    }

    public void loadLibrary(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("file");
        }

        LibraryClassLoader loader = null;
        try {
            loader = new LibraryClassLoader(this, file, this.getClass().getClassLoader());
        } catch (Throwable e) {
            throw new PluginException(String.format("Failed loading plugin in '%s'", file), e);
        }

        this.libraryLoaders.add(loader);
    }

    public int unloadPlugins() {
        ArrayList<PluginClassLoader> loaders = new ArrayList<PluginClassLoader>(this.plugins);

        for (PluginClassLoader loader : loaders) {
            this.disablePlugin(loader);
        }

        this.plugins.clear();
        this.pluginLoaders.clear();

        return loaders.size();
    }

    public void disablePlugin(PluginClassLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("plugin");
        }

        if (loader.plugin.isEnabled()) {
            loader.plugin.logger.info(String.format("Disabling plugin %s", loader.getInfo().name));

            try {
                loader.plugin.setEnabled(false);
            } catch (Throwable e) {
                loader.plugin.logger.log(Level.SEVERE, String.format("Error while disabling plugin %s", loader.getInfo().name), e);
            }

            this.pluginLoaders.remove(loader.getInfo().name);

            Set<String> classes = loader.getClasses();

            for (String clazz : classes) {
                this.removeClass(clazz);
            }

            loader.plugin.logger.info(String.format("Disabled plugin %s", loader.getInfo().name));
        }
    }

    void removeClass(String name) {
        if (this.classes.containsKey(name)) {
            this.unloadedClasses.add(name);
        }
        this.classes.remove(name);
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
        } else {
            for (String current : this.pluginLoaders.keySet()) {
                PluginClassLoader loader = this.pluginLoaders.get(current);
                try {
                    result = loader.findClass(name, false);
                } catch (ClassNotFoundException e) {
                }

                if (result != null) {
                    return result;
                }
            }

            for (LibraryClassLoader loader : this.libraryLoaders) {
                try {
                    result = loader.findClass(name, false);
                } catch (ClassNotFoundException e) {
                }

                if (result != null) {
                    return result;
                }
            }
        }

        // Ok, you're desperate
        try {
            result = this.getClass().getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    void setClass(String name, Class<?> clazz) {
        if (!this.classes.containsKey(name)) {
            this.classes.put(name, clazz);
        }
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
            } catch (Throwable e) {
                i.remove();
            }
        }
        return count;
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
