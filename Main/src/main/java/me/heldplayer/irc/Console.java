
package me.heldplayer.irc;

import java.util.logging.Level;
import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IConsole;
import me.heldplayer.irc.api.event.user.CommandEvent;
import me.heldplayer.irc.logging.FileLogHandler;

class Console implements IConsole {

    private final Logger stdout;
    private final Logger stderr;
    private FileLogHandler logfileHandler;

    public Console(Logger stdout, Logger stderr, FileLogHandler fileHandler) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.logfileHandler = fileHandler;
    }

    @Override
    public void sendMessageToConsole(String message) {
        System.out.println(message);
    }

    @Override
    public void sendMessageToConsole(String message, Object... args) {
        System.out.println(String.format(message, args));
    }

    @Override
    public void handleConsoleInput(String input) {
        if (BotAPI.eventBus.postEvent(new CommandEvent(input))) {
            BotAPI.serverConnection.addToSendQueue(input);
        }
    }

    @Override
    public void shutdown() {
        if (this.logfileHandler != null) {
            this.logfileHandler.close();
        }
        IRCBotLauncher.unloadPlugins();
        System.exit(0);
    }

    @Override
    public void log(Level level, String message) {
        if (level.intValue() > 800) {
            this.stderr.log(level, message);
        }
        else {
            this.stdout.log(level, message);
        }
    }

    @Override
    public void log(Level level, String message, Throwable thrown) {
        if (level.intValue() > 800) {
            this.stderr.log(level, message, thrown);
        }
        else {
            this.stdout.log(level, message, thrown);
        }
    }

}
