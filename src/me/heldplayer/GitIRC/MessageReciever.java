
package me.heldplayer.GitIRC;

import java.io.IOException;
import java.net.UnknownHostException;

import me.heldplayer.GitIRC.client.IRCClient;

public abstract class MessageReciever {

    protected IRCClient client;
    public static MessageReciever instance;
    public String adress;
    private long lastRead;

    public MessageReciever() {
        instance = this;
    }

    public abstract void recieve(String message);

    public abstract String getNick();

    public abstract void setNick(String newNick);

    public void send(String message) {
        message.trim();
        this.client.out.println(message);
    }

    public void init(String adress) throws UnknownHostException, IOException {
        this.adress = adress;
        this.client = new IRCClient();
        this.client.connect(adress, 6669);
        this.client.socket.setKeepAlive(true);
        this.lastRead = System.currentTimeMillis();
    }

    public void parse() throws IOException {
        if (this.lastRead + 300000L < System.currentTimeMillis()) {
            this.send("PING :" + this.adress);
            this.lastRead = System.currentTimeMillis();
        }
        while (this.client.in.ready()) {
            String message = this.client.in.readLine();
            this.recieve(message);

            this.lastRead = System.currentTimeMillis();
        }
    }
}
