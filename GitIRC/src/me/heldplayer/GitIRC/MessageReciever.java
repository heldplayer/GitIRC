package me.heldplayer.GitIRC;

import java.io.IOException;
import java.net.UnknownHostException;

import me.heldplayer.GitIRC.client.IRCClient;

public abstract class MessageReciever {

	protected IRCClient client;
	public static MessageReciever instance;
	public String adress;

	public MessageReciever() {
		instance = this;
	}

	public abstract void recieve(String message);

	public abstract String getNick();

	public abstract void setNick(String newNick);

	public void send(String message) {
		message.trim();
		client.out.println(message);
	}

	public void init(String adress) throws UnknownHostException, IOException {
		this.adress = adress;
		client = new IRCClient();
		client.connect(adress, 6669);
	}

	public void parse() throws IOException {
		if (client.socket.isClosed()) {
			throw new IOException("Connection closed");
		}
		while (client.in.ready()) {
			String message = client.in.readLine();
			recieve(message);
		}
	}
}
