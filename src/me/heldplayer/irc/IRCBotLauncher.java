
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
import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.configuration.Configuration;
import me.heldplayer.irc.logging.ConsoleLogFormatter;
import me.heldplayer.irc.logging.ConsoleLogHandler;
import me.heldplayer.irc.logging.FileLogFormatter;
import me.heldplayer.irc.logging.FileLogHandler;
import me.heldplayer.irc.logging.LoggerOutputStream;

public final class IRCBotLauncher {

    static Thread mainThread;
    static Configuration config;

    public static void main(String[] args) {
        IRCBotLauncher.config = new Configuration(new File("." + File.separator + "settings.cfg"));
        IRCBotLauncher.config.load();

        Logger stdout = Logger.getLogger("STDOUT");
        Logger stderr = Logger.getLogger("STDERR");
        Logger global = Logger.getLogger("");
        Logger rawIRC = Logger.getLogger("RawIRC");
        stdout.setUseParentHandlers(false);
        stderr.setUseParentHandlers(false);
        global.setUseParentHandlers(false);
        rawIRC.setUseParentHandlers(false);

        ConsoleLogHandler stdoutHandler = new ConsoleLogHandler(System.out);
        ConsoleLogHandler stderrHandler = new ConsoleLogHandler(System.err);
        ConsoleLogFormatter formatter = new ConsoleLogFormatter();
        stdoutHandler.setFormatter(formatter);
        stdoutHandler.setLevel(Level.FINER);
        stderrHandler.setFormatter(formatter);
        stderrHandler.setLevel(Level.FINER);

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

        new RunnableMainThread();
        mainThread = new Thread(RunnableMainThread.instance);
        mainThread.setName("Main Thread");
        mainThread.start();

        Thread consoleReader = new Thread(new RunnableConsoleReader());
        consoleReader.setName("Console reader Thread");
        consoleReader.setDaemon(true);
        consoleReader.start();
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
