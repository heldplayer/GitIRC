
package me.heldplayer.irc.api;

import me.heldplayer.irc.api.event.IEventBus;
import me.heldplayer.irc.api.plugin.PluginLoader;

public final class BotAPI {

    private BotAPI() {}

    public static long startTime;

    public static IServerConnection serverConnection;

    public static IConsole console;

    public static IEventBus eventBus;

    public static IConfiguration configuration;

    public final static PluginLoader pluginLoader = new PluginLoader();

}
