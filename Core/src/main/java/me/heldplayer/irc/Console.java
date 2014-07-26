package me.heldplayer.irc;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IConsole;
import me.heldplayer.irc.api.event.user.CommandEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

class Console implements IConsole {

    private final Logger stdout;
    private final Logger stderr;

    public Console(Logger stdout, Logger stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
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
        IRCBotLauncher.unloadPlugins();
        System.exit(0);
    }

    @Override
    public void log(Level level, String message) {
        if (level.intValue() > 800) {
            this.stderr.log(level, message);
        } else {
            this.stdout.log(level, message);
        }
    }

    @Override
    public void log(Level level, String message, Throwable thrown) {
        if (level.intValue() > 800) {
            this.stderr.log(level, message, thrown);
        } else {
            this.stdout.log(level, message, thrown);
        }
    }

}
