
package me.heldplayer.irc.api;

import me.heldplayer.irc.api.event.IEventBus;
import me.heldplayer.irc.api.plugin.PluginLoader;

public final class BotAPI {

    public static IServerConnection serverConnection;

    public static IConsole console;

    public static IEventBus eventBus;

    public final static PluginLoader pluginLoader = new PluginLoader();

}
