
package me.heldplayer.irc;

import me.heldplayer.irc.api.BotAPI;

class RunnableMainThread implements Runnable {

    public static RunnableMainThread instance;
    public boolean hasStopped;
    public boolean running;

    public boolean shouldReset;

    public RunnableMainThread() {
        RunnableMainThread.instance = this;
    }

    @Override
    public void run() {
        this.hasStopped = false;
        this.running = true;

        IRCBotLauncher.loadPlugins();

        while (this.running) {
            if (shouldReset) {
                IRCBotLauncher.unloadPlugins();
                IRCBotLauncher.loadPlugins();
                this.shouldReset = false;
                continue;
            }

            if (BotAPI.serverConnection != null) {
                BotAPI.serverConnection.processQueue();
            }

            try {
                Thread.sleep(1L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                this.running = false;
            }
        }

        this.hasStopped = true;
    }

}