
package me.heldplayer.GitIRC;

import java.io.IOException;
import java.net.UnknownHostException;

import me.heldplayer.GitIRC.client.Connection;

public abstract class MessageReciever {

    public static MessageReciever instance;
    public String adress;
    private long lastRead;
    public Connection con;

    public MessageReciever() {
        instance = this;
    }

    public void send(String message) {
        message.trim();
        this.con.out.println(message);
    }

    public void init(String adress) throws UnknownHostException, IOException {
        this.adress = adress;
        this.con = new Connection();
        this.con.connect(adress, 6669);
        this.con.socket.setKeepAlive(true);
        this.lastRead = System.currentTimeMillis();
    }

    public void parse() throws IOException {
        if (this.lastRead + 300000L < System.currentTimeMillis()) {
            this.send("PING :" + this.adress);
            this.lastRead = System.currentTimeMillis();
        }
        while (this.con.in.ready()) {
            String message = this.con.in.readLine();
            System.out.println(message);

            this.lastRead = System.currentTimeMillis();
        }
    }
}
