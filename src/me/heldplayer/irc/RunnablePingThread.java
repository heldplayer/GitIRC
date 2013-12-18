
package me.heldplayer.irc;

import me.heldplayer.irc.api.BotAPI;

class RunnablePingThread implements Runnable {

    private short counter = 0;

    @Override
    public void run() {
        try {
            while (true) {
                if (BotAPI.serverConnection != null) {
                    this.counter++;

                    if (this.counter == 60) {
                        this.counter = 0;

                        BotAPI.serverConnection.addToSendQueue("PING");
                    }
                }
                Thread.sleep(1000L);
            }
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
