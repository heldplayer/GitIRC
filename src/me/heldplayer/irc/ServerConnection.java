
package me.heldplayer.irc;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import me.heldplayer.irc.api.IServerConnection;

class ServerConnection implements IServerConnection {

    private List<String> sendQueue;
    private boolean connected;
    private boolean initialized;

    public ServerConnection() {
        this.sendQueue = Collections.synchronizedList(new LinkedList<String>());
        this.connected = false;
        this.initialized = false;
    }

    @Override
    public void addToSendQueue(String command) {
        this.sendQueue.add(command);
    }

    @Override
    public void processQueue() {
        if (!connected && IRCBotLauncher.connection.socket.isConnected()) {
            connected = true;
        }
        Iterator<String> iterator = this.sendQueue.iterator();
        long incremental = 0L;
        while (iterator.hasNext()) {
            String command = iterator.next();

            IRCBotLauncher.connection.out.println(command.trim());

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
