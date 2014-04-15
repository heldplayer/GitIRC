
package me.heldplayer.web.server.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import me.heldplayer.web.server.WebServerEntryPoint;
import me.heldplayer.web.server.internal.security.AccessManager;

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

        instance = this;
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
            for (int i = 0; i < runningRequests.size(); i++) {
                RunnableHttpResponse response = runningRequests.get(i);
                if (response.finished) {
                    runningRequests.remove(i);
                }
            }
            if (runningRequests.isEmpty()) {
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

        instance = null;
    }

    @Override
    public void run() {
        try {
            WebServerEntryPoint.log.info("Starting server on " + (this.host != null && !this.host.isEmpty() ? this.host : "*") + ":" + this.port);

            InetAddress adress = null;

            if (this.host != null && !this.host.isEmpty()) {
                InetAddress.getByName(this.host);
            }

            this.serverSocket = new ServerSocket(this.port, 0, adress);
        }
        catch (Exception ex) {
            WebServerEntryPoint.log.severe("**** FAILED TO BIND TO PORT");
            WebServerEntryPoint.log.severe("The exception was: " + ex.toString());
            WebServerEntryPoint.log.severe("Perhaps something is already running on that port?");
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
                runningRequests.add(response);

                Thread.sleep(10L);
            }
            catch (IOException e) {}
            catch (InterruptedException e) {}
        }

        this.hasStopped = true;
    }
}
