
package me.heldplayer.irc.api.plugin;

import java.io.File;
import java.net.MalformedURLException;

import me.heldplayer.irc.api.sandbox.SandboxBlacklist;

@SandboxBlacklist
public class LibraryClassLoader extends CustomClassLoader {

    public LibraryClassLoader(PluginLoader loader, File file, ClassLoader parent) throws MalformedURLException {
        super(loader, file, parent, file.getName());
    }

}
