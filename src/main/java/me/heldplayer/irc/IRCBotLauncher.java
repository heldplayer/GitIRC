
package me.heldplayer.irc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IEntryPoint;
import me.heldplayer.irc.api.configuration.Configuration;
import me.heldplayer.irc.logging.ConsoleLogFormatter;
import me.heldplayer.irc.logging.ConsoleLogHandler;
import me.heldplayer.irc.logging.FileLogFormatter;
import me.heldplayer.irc.logging.FileLogHandler;
import me.heldplayer.irc.logging.LoggerOutputStream;

public final class IRCBotLauncher {

    static Thread mainThread;
    protected static Configuration config;

    private static final Logger log = Logger.getLogger("API");
    private static boolean pluginsLoaded = false;
    private static ArrayList<IEntryPoint> pluginEntryPoints = new ArrayList<IEntryPoint>();

    private static PrintStream stdOut;
    private static PrintStream stdErr;

    public static void main(String[] args) {
        IRCBotLauncher.config = new Configuration(new File("." + File.separator + "settings.cfg"));
        IRCBotLauncher.config.load();

        Thread consoleReader = new Thread(new RunnableConsoleReader());
        consoleReader.setName("Console reader Thread");
        consoleReader.setDaemon(true);
        consoleReader.start();

        new RunnableMainThread();
        mainThread = new Thread(RunnableMainThread.instance);
        mainThread.setName("Main Thread");
        mainThread.start();
    }

    private static void setupLoggers() {
        stdOut = System.out;
        stdErr = System.err;

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
        ConsoleLogHandler stderrHandler = new ConsoleLogHandler(System.err);
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

            fileHandler = new FileLogHandler(IRCBotLauncher.config.getString("log-file"), true);
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

        System.setOut(stdOut);
        System.setErr(stdErr);
        LogManager.getLogManager().reset();
    }

    public static void loadPlugins() {
        if (pluginsLoaded) {
            log.info("Tried loading but already loaded");
            return;
        }

        setupLoggers();

        BotAPI.eventBus = new EventBus();

        String serverIp = IRCBotLauncher.config.getString("server-ip");
        int serverPort = IRCBotLauncher.config.getInt("server-port");
        String bindHost = IRCBotLauncher.config.getString("bind-host");
        String nickname = IRCBotLauncher.config.getString("nickname");

        ServerConnection connection = new ServerConnection(serverIp, serverPort, bindHost);
        BotAPI.serverConnection = connection;
        BotAPI.eventBus.registerEventHandler(BotAPI.serverConnection);

        try {
            connection.connect(nickname);
        }
        catch (Throwable e) {
            BotAPI.console.log(Level.SEVERE, "Failed connecting to the server", e);
        }

        log.info("Looking around for internal plugins to load");

        int count = 0;

        try {
            Enumeration<URL> entries = IRCBotLauncher.class.getClassLoader().getResources("bot/entries");

            final List<File> files = new ArrayList<File>();

            FileFilter filter = new FileFilter() {

                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        file.listFiles(this);
                    }
                    files.add(file);
                    return true;
                }

            };

            while (entries.hasMoreElements()) {
                URL entry = entries.nextElement();

                try {
                    File file = new File(entry.toURI());

                    file.listFiles(filter);
                }
                catch (URISyntaxException e) {}
            }

            for (File file : files) {
                log.info("Loading entry " + file.getPath());

                BufferedReader in = null;
                try {
                    in = new BufferedReader(new FileReader(file));

                    while (in.ready()) {
                        String line = in.readLine();

                        Class<?> clazz = IRCBotLauncher.class.getClassLoader().loadClass(line);

                        if (IEntryPoint.class.isAssignableFrom(clazz)) {
                            IEntryPoint entryPoint = (IEntryPoint) clazz.newInstance();
                            IRCBotLauncher.pluginEntryPoints.add(entryPoint);
                            count++;
                        }
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException("Failed loading entry", e);
                }
                finally {
                    in.close();
                }

            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed loading entry", e);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed loading class entry", e);
        }
        catch (InstantiationException e) {
            throw new RuntimeException("Failed loading class entry", e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Failed loading class entry", e);
        }

        log.info("Found " + count + " internal plugins");

        for (IEntryPoint entry : pluginEntryPoints) {
            entry.load();
        }

        log.info("Loaded " + pluginEntryPoints.size() + " plugins");

        pluginsLoaded = true;

        // TODO: add external plugin loading
    }

    public static void unloadPlugins() {
        if (!pluginsLoaded) {
            log.info("Tried to unload but not loaded yet");
            return;
        }

        if (BotAPI.serverConnection.isConnected()) {
            BotAPI.serverConnection.disconnect("Rehashing...");
        }

        BotAPI.eventBus.cleanup();

        BotAPI.serverConnection = null;
        BotAPI.eventBus = null;

        for (IEntryPoint entry : pluginEntryPoints) {
            entry.unload();
        }

        log.info("Unloaded " + pluginEntryPoints.size() + " plugins");

        resetLoggers();

        pluginEntryPoints.clear();

        System.gc();

        pluginsLoaded = false;
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
