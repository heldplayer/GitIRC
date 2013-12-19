
package me.heldplayer.irc;

import java.util.logging.Logger;

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

    }

}
