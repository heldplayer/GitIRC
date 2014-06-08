
package me.heldplayer.irc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.configuration.ConfigurationException;
import me.heldplayer.irc.logging.ConsoleLogFormatter;
import me.heldplayer.irc.logging.ConsoleLogHandler;
import me.heldplayer.irc.logging.FileLogFormatter;
import me.heldplayer.irc.logging.FileLogHandler;
import me.heldplayer.irc.logging.LoggerOutputStream;

public final class IRCBotLauncher {

    static Thread mainThread;

    private static final Logger log = Logger.getLogger("API");
    private static boolean pluginsLoaded = false;

    private static PrintStream stdOut;
    private static PrintStream stdErr;

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

    private static void setupLoggers() {
        if (BotAPI.console != null) {
            IRCBotLauncher.resetLoggers();
        }
        IRCBotLauncher.stdOut = System.out;
        IRCBotLauncher.stdErr = System.err;

        Logger stdout = Logger.getLogger("STDOUT");
        Logger stderr = Logger.getLogger("STDERR");
        Logger global = Logger.getLogger("");
        Logger rawIRC = Logger.getLogger("RawIRC");
        stdout.setUseParentHandlers(false);
        stderr.setUseParentHandlers(false);
        global.setUseParentHandlers(false);
        rawIRC.setUseParentHandlers(false);

        // Disable stupid logger
        Logger httpURLConnection = Logger.getLogger("sun.net.www.protocol.http.HttpURLConnection");
        httpURLConnection.setLevel(Level.OFF);

        ConsoleLogHandler stdoutHandler = new ConsoleLogHandler(System.out);
        ConsoleLogHandler stderrHandler = new ConsoleLogHandler(System.out);
        ConsoleLogFormatter formatter = new ConsoleLogFormatter();
        stdoutHandler.setFormatter(formatter);
        stdoutHandler.setLevel(Level.INFO);
        stderrHandler.setFormatter(formatter);
        stderrHandler.setLevel(Level.INFO);

        stdout.addHandler(stdoutHandler);
        stdout.setLevel(Level.ALL);
        stderr.addHandler(stderrHandler);
        stderr.setLevel(Level.ALL);

        for (Handler handler : global.getHandlers()) {
            global.removeHandler(handler);
        }
        global.addHandler(stdoutHandler);
        global.setLevel(Level.ALL);

        FileLogHandler fileHandler = null;
        try {
            fileHandler = new FileLogHandler("./raw.log", true);
            fileHandler.setFormatter(new FileLogFormatter());
            fileHandler.setLevel(Level.ALL);
            rawIRC.addHandler(fileHandler);

            fileHandler = new FileLogHandler(BotAPI.configuration.getLogFile(), true);
            fileHandler.setFormatter(new FileLogFormatter());
            fileHandler.setLevel(Level.ALL);
            stdout.addHandler(fileHandler);
            stderr.addHandler(fileHandler);
            global.addHandler(fileHandler);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        System.setOut(new PrintStream(new LoggerOutputStream(stdout, Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(stderr, Level.WARNING), true));

        BotAPI.console = new Console(stdout, stderr, fileHandler);
    }

    private static void resetLoggers() {
        BotAPI.console = null;

        System.setOut(IRCBotLauncher.stdOut);
        System.setErr(IRCBotLauncher.stdErr);
        LogManager.getLogManager().reset();
    }

    public static void loadPlugins() {
        if (IRCBotLauncher.pluginsLoaded) {
            throw new RuntimeException("Plugins already loaded");
        }

        BotAPI.configuration = new BotConfiguration();

        IRCBotLauncher.setupLoggers();

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
        }
        catch (Throwable e) {
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

        IRCBotLauncher.resetLoggers();

        BotAPI.configuration = null;

        System.gc();

        IRCBotLauncher.pluginsLoaded = false;
    }

    public static List<String> readPerform() {
        ArrayList<String> result = new ArrayList<String>();

        File perform = new File("." + File.separator + "perform.txt");
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
        }
        catch (IOException e) {}
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (IOException e) {}
        }

        return result;
    }

}
