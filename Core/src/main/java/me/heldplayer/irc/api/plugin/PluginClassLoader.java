
package me.heldplayer.irc.api.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import me.heldplayer.irc.api.sandbox.SandboxBlacklist;

@SandboxBlacklist
public class PluginClassLoader extends CustomClassLoader {

    PluginInfo info;
    Plugin plugin;

    public PluginClassLoader(PluginLoader loader, File file, PluginInfo info, ClassLoader parent) throws MalformedURLException {
        super(loader, file, parent, info.name);

        this.info = info;
    }

    void initializePlugin() {
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

}
