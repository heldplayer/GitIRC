
package me.heldplayer.GitIRC;

public class ThreadPing extends Thread {
    private ConsoleMessageReciever reciever;
    private byte pos = 0;
    private static boolean launched = false;

    public ThreadPing(ConsoleMessageReciever parent) {
        super("Ping thread");
        reciever = parent;

        launched = !launched;
    }

    public void run() {
        if (!launched) {
            launched = true;
            return;
        }

        try {
            while (reciever.isRunning()) {
                pos++;

                if (pos == 60) {
                    pos = 0;

                    reciever.inputBuffer.put(reciever.index++, "PING");
                }

                Thread.sleep(1000L);
            }
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
