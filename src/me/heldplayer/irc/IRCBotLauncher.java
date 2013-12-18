
package me.heldplayer.irc;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.configuration.Configuration;

final class IRCBotLauncher {

    static Thread mainThread;
    static IRCConnection connection;
    static Configuration config;

    public static void main(String[] args) {
        IRCBotLauncher.config = new Configuration(new File("." + File.pathSeparator + "settings.cfg"));
        IRCBotLauncher.config.load();

        String serverIp = IRCBotLauncher.config.getString("server-ip");
        int serverPort = IRCBotLauncher.config.getInt("server-port");
        String bindHost = IRCBotLauncher.config.getString("bind-host");

        IRCBotLauncher.connection = new IRCConnection();
        try {
            IRCBotLauncher.connection.connect(serverIp, serverPort, bindHost);
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        BotAPI.serverConnection = new ServerConnection();

        new RunnableMainThread();
        mainThread = new Thread(RunnableMainThread.instance);
        mainThread.setName("Main Thread");
        mainThread.start();
    }

}
