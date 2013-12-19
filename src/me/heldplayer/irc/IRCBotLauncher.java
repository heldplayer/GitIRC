
package me.heldplayer.irc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
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

        String serverIp = IRCBotLauncher.config.getString("server-ip");
        int serverPort = IRCBotLauncher.config.getInt("server-port");
        String bindHost = IRCBotLauncher.config.getString("bind-host");

        Logger stdout = Logger.getLogger("STDOUT");
        Logger stderr = Logger.getLogger("STDERR");
        stdout.setUseParentHandlers(false);
        stderr.setUseParentHandlers(false);

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

        System.setOut(new PrintStream(new LoggerOutputStream(stdout, Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(stderr, Level.WARNING), true));

        System.setSecurityManager(null);

        System.out.println("Test");
        System.err.println("Test2");

        BotAPI.console = new Console(stdout, stderr);
        ServerConnection connection = new ServerConnection();
        BotAPI.serverConnection = connection;

        try {
            connection.connect(serverIp, serverPort, bindHost);
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        new RunnableMainThread();
        mainThread = new Thread(RunnableMainThread.instance);
        mainThread.setName("Main Thread");
        mainThread.start();
    }

}
