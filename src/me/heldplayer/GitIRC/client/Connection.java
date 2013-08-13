
package me.heldplayer.GitIRC.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection {

    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;

    public void connect(String host, int port) throws UnknownHostException, IOException {
        InetSocketAddress adrr = new InetSocketAddress(host, port);

        this.socket = new Socket(adrr.getAddress(), adrr.getPort());
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

}
