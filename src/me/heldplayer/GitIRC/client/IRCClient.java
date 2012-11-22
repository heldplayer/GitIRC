package me.heldplayer.GitIRC.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class IRCClient {
	public Socket socket;
	public PrintWriter out;
	public BufferedReader in;

	public IRCClient() {
	}

	public void connect(String host, int port) throws UnknownHostException, IOException {
		InetSocketAddress adrr = new InetSocketAddress(host, port);

		socket = new Socket(adrr.getAddress(), adrr.getPort());
		//socket = new Socket(host, port);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
}
