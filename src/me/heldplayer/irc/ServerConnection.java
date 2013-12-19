
package me.heldplayer.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import me.heldplayer.irc.api.IServerConnection;

class ServerConnection implements IServerConnection {

    private List<String> sendQueue;
    private boolean connected;
    private boolean initialized;

    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;

    public ServerConnection() {
        this.sendQueue = Collections.synchronizedList(new LinkedList<String>());
        this.connected = false;
        this.initialized = false;
    }

    public void connect(String host, int port, String localHost) throws UnknownHostException, IOException {
        InetAddress remote = InetAddress.getByName(host);

        if (localHost != null && !localHost.isEmpty()) {
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

    @Override
    public void addToSendQueue(String command) {
        this.sendQueue.add(command);
    }

    @Override
    public void processQueue() {
        if (!connected && this.socket.isConnected()) {
            connected = true;
        }
        Iterator<String> iterator = this.sendQueue.iterator();
        long incremental = 0L;
        while (iterator.hasNext()) {
            String command = iterator.next();

            this.out.println(command.trim());

            try {
                Thread.sleep(250L * incremental);
            }
            catch (InterruptedException e) {}
            incremental++;

            iterator.remove();
        }
    }

    @Override
    public String getServerName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNickname() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNickname(String nickname) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnect(String reason) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public boolean isLoggedIn() {
        return this.initialized;
    }

}
