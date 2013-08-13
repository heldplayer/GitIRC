
package me.heldplayer.GitIRC;

public class ThreadPing extends Thread {
    private ConsoleMessageReciever reciever;
    private byte pos = 0;
    private static boolean launched = false;

    public ThreadPing(ConsoleMessageReciever parent) {
        super("Ping thread");
        this.reciever = parent;

        launched = !launched;
    }

    @Override
    public void run() {
        if (!launched) {
            launched = true;
            return;
        }

        try {
            while (this.reciever.isRunning()) {
                this.pos++;

                if (this.pos == 60) {
                    this.pos = 0;

                    this.reciever.inputBuffer.add("PING");
                }

                Thread.sleep(1000L);
            }
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
