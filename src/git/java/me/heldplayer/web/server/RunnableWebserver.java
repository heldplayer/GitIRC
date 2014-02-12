
package me.heldplayer.web.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class RunnableWebserver implements Runnable {

    protected static Logger log = Logger.getLogger("Web");
    public static RunnableWebserver instance;

    private ServerSocket serverSocket = null;
    public boolean running = false;

    private final int port;
    private final String host;

    public RunnableWebserver(int port, String host) {
        super();

        instance = this;
        this.port = port;
        this.host = host;
    }

    public synchronized void disconnect() {
        running = false;
        try {
            serverSocket.close();
        }
        catch (IOException e) {}
    }

    @Override
    public void run() {
        try {
            RunnableWebserver.log.info("Starting server on " + (host != "" ? host : "*") + ":" + port);

            InetAddress adress = null;

            if (host.length() > 0) {
                InetAddress.getByName(host);
            }

            serverSocket = new ServerSocket(port, 0, adress);
        }
        catch (Exception ex) {
            RunnableWebserver.log.severe("**** FAILED TO BIND TO PORT");
            RunnableWebserver.log.severe("The exception was: " + ex.toString());
            RunnableWebserver.log.severe("Perhaps something is already running on that port?");
            return;
        }

        while (running) {
            try {
                Socket socket = serverSocket.accept();

                new RunnableHttpResponse(socket);

                Thread.sleep(10L);
            }
            catch (IOException e) {}
            catch (InterruptedException e) {}
        }
    }
}
