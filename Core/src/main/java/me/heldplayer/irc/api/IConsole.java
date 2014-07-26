package me.heldplayer.irc.api;

import java.util.logging.Level;

public interface IConsole {

    void sendMessageToConsole(String message);

    void sendMessageToConsole(String message, Object... args);

    void handleConsoleInput(String input);

    void shutdown();

    void log(Level level, String message);

    void log(Level level, String message, Throwable thrown);

}
