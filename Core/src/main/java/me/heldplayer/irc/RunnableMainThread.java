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
        BotAPI.startTime = System.currentTimeMillis();

        IRCBotLauncher.loadPlugins();

        while (this.running) {
            if (this.shouldReset) {
                IRCBotLauncher.unloadPlugins();
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    this.running = false;
                }
                IRCBotLauncher.loadPlugins();
                this.shouldReset = false;
                continue;
            }

            if (BotAPI.serverConnection != null) {
                BotAPI.serverConnection.processQueue();
            }

            try {
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                this.running = false;
            }
        }

        this.hasStopped = true;
    }

}
