
package me.heldplayer.GitIRC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import me.heldplayer.GitIRC.client.IncomingMessage;

public class ConsoleMessageReciever extends MessageReciever {

    public static ConsoleMessageReciever instance;
    private boolean running = false;
    private final BufferedReader in;
    private String nick;
    public volatile HashMap<Integer, String> inputBuffer;
    public int index = 0;
    public ThreadCommitReader commitReader;

    public ConsoleMessageReciever() {
        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.inputBuffer = new HashMap<Integer, String>();
    }

    @Override
    public void recieve(String message) {
        IncomingMessage msg = new IncomingMessage(message);

        if (!msg.parse(this)) {
            System.out.println(message);
        }
    }

    @Override
    public String getNick() {
        return this.nick;
    }

    @Override
    public void setNick(String newNick) {
        this.nick = newNick;
        this.send("NICK " + this.nick);
    }

    public void stop() throws IOException {
        this.running = false;
        this.client.in.close();
        this.client.out.close();
        this.client.socket.close();
    }

    public void init() throws IOException {
        System.out.print("Enter an IRC server to connect to: ");

        String adress = this.in.readLine();

        System.out.print("Enter a nickname: ");

        this.nick = this.in.readLine();

        super.init(adress);

        this.send("CAP LS");
        this.send("NICK " + this.nick);
        this.send("USER GitIRC 0 * :" + this.nick);

        this.running = true;
    }

    @Override
    public void parse() throws IOException {
        synchronized (this.inputBuffer) {
            for (Entry<Integer, String> entry : this.inputBuffer.entrySet()) {
                String command = entry.getValue();

                if (command.startsWith("/join")) {
                    this.send("JOIN " + command.split(" ")[1]);
                    continue;
                }
                else if (command.startsWith("/me")) {
                    String[] args = command.split(" ");

                    String result = "";
                    for (int i = 2; i < args.length; i++) {
                        if (i != 2) {
                            result += " ";
                        }
                        result += args[i];
                    }

                    this.send("PRIVMSG " + args[1] + " :\u0001ACTION " + result + "\u0001");
                    System.out.println("[" + args[1] + "] * " + this.nick + " " + result);
                    continue;
                }
                else if (command.startsWith("/say")) {
                    String[] args = command.split(" ");

                    String result = "";
                    for (int i = 2; i < args.length; i++) {
                        if (i != 2) {
                            result += " ";
                        }
                        result += args[i];
                    }

                    this.send("PRIVMSG " + args[1] + " :" + result);
                    System.out.println("[" + args[1] + "] <" + this.nick + "> " + result);
                    continue;
                }
                else if (command.startsWith("/quit")) {
                    String[] args = command.split(" ");

                    String result = "";
                    for (int i = 1; i < args.length; i++) {
                        if (i != 1) {
                            result += " ";
                        }
                        result += args[i];
                    }

                    this.send("QUIT :" + result);
                    this.stop();
                    return;
                }
                else if (command.startsWith("/setupbot")) {
                    if (!ThreadCommitReader.launched) {
                        this.commitReader = new ThreadCommitReader(this, command.substring(10));
                        this.commitReader.setDaemon(true);
                        this.commitReader.start();

                        System.out.println("Started commit reading thread");

                        continue;
                    }
                }
                else if (command.startsWith("/changechan")) {
                    if (this.commitReader != null) {
                        this.commitReader.chan = command.substring(12);
                    }

                    System.out.println("Changed commit reading thread output");

                    continue;
                }

                this.send(command);

                try {
                    Thread.sleep(500L);
                }
                catch (InterruptedException e) {}
            }

            this.inputBuffer.clear();

            super.parse();
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public static void main(String[] args) {
        instance = new ConsoleMessageReciever();

        try {
            instance.init();
        }
        catch (IOException ex) {
            System.err.println("Unexpected IO error! Stopping!");
            ex.printStackTrace();
            try {
                instance.stop();
            }
            catch (IOException e) {}
        }

        ThreadCommandReader commandReader = new ThreadCommandReader(instance);
        commandReader.setDaemon(true);
        commandReader.start();

        while (instance.running) {
            try {
                instance.parse();
                Thread.sleep(1L);
            }
            catch (InterruptedException ex) {}
            catch (Exception ex) {
                System.err.println("Unexpected error! Stopping!");
                ex.printStackTrace();
                try {
                    instance.stop();
                }
                catch (IOException e) {}
            }
        }

        System.out.println("Disconneted.");
    }
}
