
package me.heldplayer.GitIRC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ThreadCommandReader extends Thread {
    private ConsoleMessageReciever reciever;

    public ThreadCommandReader(ConsoleMessageReciever parent) {
        super("Console command reader");
        this.reciever = parent;
    }

    @Override
    public void run() {
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in));
        String s = null;

        try {
            while (this.reciever.isRunning() && (s = bufferedreader.readLine()) != null) {
                this.reciever.inputBuffer.put(this.reciever.index++, s);
            }
        }
        catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }
}
