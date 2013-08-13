
package me.heldplayer.GitIRC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class ThreadCommitReader extends Thread {
    private ConsoleMessageReciever reciever;
    private byte pos = 0;
    protected static boolean launched = false;
    protected String chan;

    public ThreadCommitReader(ConsoleMessageReciever parent, String channel) {
        super("Commit reader");
        this.reciever = parent;
        this.chan = channel;

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

                    try {
                        URL changes = new URL("http://dsiwars.x10.mx/Git/retrieve.php");
                        BufferedReader in = new BufferedReader(new InputStreamReader(changes.openStream()));

                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            if (!inputLine.startsWith("#")) {
                                continue;
                            }

                            if (inputLine.equalsIgnoreCase("0")) {
                                break;
                            }
                            synchronized (this.reciever.inputBuffer) {
                                this.reciever.inputBuffer.add("/say " + this.chan + " " + inputLine.substring(1));
                            }
                            Thread.sleep(500L);
                        }
                        in.close();
                    }
                    catch (Exception ex) {}
                }

                Thread.sleep(1000L);
            }
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
