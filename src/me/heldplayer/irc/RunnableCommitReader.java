
package me.heldplayer.irc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import me.heldplayer.irc.api.BotAPI;

class RunnableCommitReader implements Runnable {

    private short counter = 50;
    private String channel;
    public boolean running = true;

    public RunnableCommitReader(String channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        try {
            while (running) {
                if (BotAPI.serverConnection != null) {
                    if (this.counter == 60) {
                        this.counter = 0;

                        try {
                            URL changes = new URL("http://dsiwars.specialattack.net/Git/retrieve.php");
                            BufferedReader in = new BufferedReader(new InputStreamReader(changes.openStream()));

                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                if (!inputLine.startsWith("#")) {
                                    continue;
                                }

                                if (inputLine.equalsIgnoreCase("0")) {
                                    break;
                                }

                                BotAPI.serverConnection.addToSendQueue("PRIVMSG " + channel + " :" + inputLine.substring(1));
                            }
                            in.close();
                        }
                        catch (Exception ex) {}
                    }

                    this.counter++;

                    Thread.sleep(1000L);
                }
            }
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
