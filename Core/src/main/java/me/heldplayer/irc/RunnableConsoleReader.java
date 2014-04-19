
package me.heldplayer.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import me.heldplayer.irc.api.BotAPI;

class RunnableConsoleReader implements Runnable {

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = null;

        try {
            while ((input = reader.readLine()) != null) {
                BotAPI.console.handleConsoleInput(input);
            }
        }
        catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

}
