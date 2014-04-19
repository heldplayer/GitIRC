
package me.heldplayer.irc.git.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import me.heldplayer.irc.git.GitPlugin;
import me.heldplayer.irc.git.internal.security.AccessManager;

public class RunnableWebserver implements Runnable {

    public static RunnableWebserver instance;

    private ServerSocket serverSocket = null;
    public boolean running = false;
    public boolean hasStopped = false;

    private final int port;
    private final String host;

    private ThreadGroup threads;
    private ArrayList<RunnableHttpResponse> runningRequests;

    public AccessManager accessManager;

    public RunnableWebserver(int port, String host) {
        super();

        RunnableWebserver.instance = this;
        this.port = port;
        this.host = host;

        this.threads = new ThreadGroup("HTTP Responses");
        this.runningRequests = new ArrayList<RunnableHttpResponse>();

        this.accessManager = new AccessManager();
    }

    @SuppressWarnings("deprecation")
    public synchronized void disconnect() {
        this.running = false;

        while (true) {
            for (int i = 0; i < this.runningRequests.size(); i++) {
                RunnableHttpResponse response = this.runningRequests.get(i);
                if (response.finished) {
                    this.runningRequests.remove(i);
                }
            }
            if (this.runningRequests.isEmpty()) {
                break;
            }
        }

        this.threads.stop();
        this.threads.destroy();
        try {
            this.serverSocket.close();
        }
        catch (IOException e) {}

        this.accessManager.cleanup();

        RunnableWebserver.instance = null;
    }

    @Override
    public void run() {
        try {
            GitPlugin.getLog().info("Starting server on " + (this.host != null && !this.host.isEmpty() ? this.host : "*") + ":" + this.port);

            InetAddress adress = null;

            if (this.host != null && !this.host.isEmpty()) {
                InetAddress.getByName(this.host);
            }

            this.serverSocket = new ServerSocket(this.port, 0, adress);
        }
        catch (Exception ex) {
            GitPlugin.getLog().severe("**** FAILED TO BIND TO PORT");
            GitPlugin.getLog().severe("The exception was: " + ex.toString());
            GitPlugin.getLog().severe("Perhaps something is already running on that port?");
            return;
        }

        this.running = true;

        while (this.running) {
            try {
                Socket socket = this.serverSocket.accept();

                RunnableHttpResponse response = new RunnableHttpResponse(socket);
                Thread responseThread = new Thread(this.threads, response);
                responseThread.setDaemon(true);
                responseThread.start();
                this.runningRequests.add(response);

                Thread.sleep(10L);
            }
            catch (IOException e) {}
            catch (InterruptedException e) {}
        }

        this.hasStopped = true;
    }
}
