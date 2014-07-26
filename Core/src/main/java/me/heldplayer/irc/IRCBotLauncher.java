package me.heldplayer.irc;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.configuration.ConfigurationException;
import me.heldplayer.irc.api.sandbox.SandboxBlacklist;
import net.specialattack.loader.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SandboxBlacklist
@Service
public final class IRCBotLauncher {

    private static final Logger log = Logger.getLogger("API");
    public static File rootDirectory = new File("." + File.separator + "GitIRC");
    static Thread mainThread;
    private static boolean pluginsLoaded = false;

    public static void startService() {
        new RunnableMainThread();
        IRCBotLauncher.mainThread = new Thread(RunnableMainThread.instance);
        IRCBotLauncher.mainThread.setName("Main Thread");
        IRCBotLauncher.mainThread.start();
    }

    public static void stopService() {

    }

    public static void main(String[] args) {
        Thread consoleReader = new Thread(new RunnableConsoleReader());
        consoleReader.setName("Console reader Thread");
        consoleReader.setDaemon(true);
        consoleReader.start();

        new RunnableMainThread();
        IRCBotLauncher.mainThread = new Thread(RunnableMainThread.instance);
        IRCBotLauncher.mainThread.setName("Main Thread");
        IRCBotLauncher.mainThread.start();
    }

    public static void loadPlugins() {
        if (IRCBotLauncher.pluginsLoaded) {
            throw new RuntimeException("Plugins already loaded");
        }

        BotAPI.configuration = new BotConfiguration();

        BotAPI.console = new Console(Logger.getLogger("IRC-OUT"), Logger.getLogger("IRC-ERR"));

        IRCBotLauncher.log.info("Loaded " + BotAPI.pluginLoader.loadLibraries() + " libraries");

        BotAPI.eventBus = new EventBus();

        String serverIp = BotAPI.configuration.getServerIp();
        int serverPort = BotAPI.configuration.getServerPort();
        String bindHost = BotAPI.configuration.getBindHost();
        String nickname = BotAPI.configuration.getNickname();

        if (serverIp.isEmpty()) {
            throw new ConfigurationException("Server IP is missing from the configuration");
        }
        if (serverPort <= 0 || serverPort >= 65535) {
            throw new ConfigurationException("Server port must be between 0 and 65535");
        }
        if (nickname.isEmpty()) {
            throw new ConfigurationException("Nickname must not be empty");
        }

        ServerConnection connection = new ServerConnection(serverIp, serverPort, bindHost);
        BotAPI.serverConnection = connection;
        BotAPI.eventBus.registerEventHandler(BotAPI.serverConnection);

        try {
            connection.connect(nickname);
        } catch (Throwable e) {
            BotAPI.console.log(Level.SEVERE, "Failed connecting to the server", e);
            BotAPI.console.shutdown();
        }

        IRCBotLauncher.log.info("Loaded " + BotAPI.pluginLoader.loadPlugins() + " plugins");

        IRCBotLauncher.pluginsLoaded = true;
    }

    public static void unloadPlugins() {
        if (!IRCBotLauncher.pluginsLoaded) {
            return;
        }

        if (BotAPI.serverConnection.isConnected()) {
            BotAPI.serverConnection.disconnect("Rehashing...");
            BotAPI.serverConnection.processQueue();
        }

        IRCBotLauncher.log.info("Unloaded " + BotAPI.pluginLoader.unloadPlugins() + " plugins");

        BotAPI.eventBus.cleanup();

        BotAPI.serverConnection = null;
        BotAPI.eventBus = null;

        IRCBotLauncher.log.info("Unloaded " + BotAPI.pluginLoader.unloadLibraries() + " libraries");
        IRCBotLauncher.log.info("There are " + BotAPI.pluginLoader.getLoadedClassesCount() + " loaded classes remaining");
        System.gc();
        IRCBotLauncher.log.info("There are " + BotAPI.pluginLoader.getUnloadingClassesCount() + " classes pending for unload");

        BotAPI.console = null;

        BotAPI.configuration = null;

        System.gc();

        IRCBotLauncher.pluginsLoaded = false;
    }

    public static List<String> readPerform() {
        ArrayList<String> result = new ArrayList<String>();

        File perform = new File(IRCBotLauncher.rootDirectory, "perform.txt");
        if (!perform.exists()) {
            return result;
        }

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(perform));
            if (reader != null) {
                String line = "";

                while ((line = reader.readLine()) != null) {
                    result.add(line);
                }
            }
        } catch (IOException e) {
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        return result;
    }

}
