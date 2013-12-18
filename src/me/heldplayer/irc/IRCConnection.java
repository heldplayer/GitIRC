
package me.heldplayer.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

class IRCConnection {

    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;

    public void connect(String host, int port, String localHost) throws UnknownHostException, IOException {
        InetAddress remote = InetAddress.getByName(host);

        if (localHost == null || localHost.isEmpty()) {
            InetAddress local = InetAddress.getByName(localHost);
            this.socket = new Socket(remote, port, local, 0);
        }
        else {
            this.socket = new Socket(remote, port);
        }

        this.socket.setKeepAlive(true);
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
    }

}
