
package me.heldplayer.irc;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.configuration.Configuration;
import me.heldplayer.irc.logging.ConsoleLogHandler;
import me.heldplayer.irc.logging.LogFormatter;
import me.heldplayer.irc.logging.LoggerOutputStream;

final class IRCBotLauncher {

    static Thread mainThread;
    static Configuration config;

    public static void main(String[] args) {
        IRCBotLauncher.config = new Configuration(new File("." + File.separator + "settings.cfg"));
        IRCBotLauncher.config.load();

        Logger stdout = Logger.getLogger("STDOUT");
        Logger stderr = Logger.getLogger("STDERR");
        Logger global = Logger.getLogger("");
        stdout.setUseParentHandlers(false);
        stderr.setUseParentHandlers(false);
        global.setUseParentHandlers(false);

        ConsoleLogHandler stdoutHandler = new ConsoleLogHandler(System.out);
        ConsoleLogHandler stderrHandler = new ConsoleLogHandler(System.err);
        LogFormatter formatter = new LogFormatter();
        stdoutHandler.setFormatter(formatter);
        stdoutHandler.setLevel(Level.ALL);
        stderrHandler.setFormatter(formatter);
        stderrHandler.setLevel(Level.ALL);

        stdout.addHandler(stdoutHandler);
        stdout.setLevel(Level.ALL);
        stderr.addHandler(stderrHandler);
        stderr.setLevel(Level.ALL);

        for (Handler handler : global.getHandlers()) {
            global.removeHandler(handler);
        }
        global.addHandler(stdoutHandler);
        global.setLevel(Level.ALL);

        System.setOut(new PrintStream(new LoggerOutputStream(stdout, Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(stderr, Level.WARNING), true));

        BotAPI.console = new Console(stdout, stderr);
        BotAPI.eventBus = new EventBus();
        ServerConnection connection = new ServerConnection();
        BotAPI.serverConnection = connection;

        String serverIp = IRCBotLauncher.config.getString("server-ip");
        int serverPort = IRCBotLauncher.config.getInt("server-port");
        String bindHost = IRCBotLauncher.config.getString("bind-host");
        String nickname = IRCBotLauncher.config.getString("nickname");

        try {
            connection.connect(serverIp, serverPort, bindHost, nickname);
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

}
