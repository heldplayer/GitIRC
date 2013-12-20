
package me.heldplayer.irc;

import java.util.logging.Level;
import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IConsole;

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
    public void handleConsoleInput(String input) {
        // TODO Auto-generated method stub
        BotAPI.serverConnection.addToSendQueue(input);
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
