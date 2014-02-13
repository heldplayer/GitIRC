
package me.heldplayer.web.server;

import java.io.File;

import me.heldplayer.irc.api.IEntryPoint;
import me.heldplayer.irc.api.configuration.Configuration;
import me.heldplayer.web.server.internal.RunnableWebserver;

public class WebServerEntryPoint implements IEntryPoint {

    public static Configuration config;

    private RunnableWebserver webServer;
    private Thread webServerThread;

    @Override
    public void load() {
        WebServerEntryPoint.config = new Configuration(new File("." + File.separator + "webserver.cfg"));
        WebServerEntryPoint.config.load();

        String bindhost = WebServerEntryPoint.config.getString("bind-host");
        int port = WebServerEntryPoint.config.getInt("port");

        this.webServer = new RunnableWebserver(port, bindhost);
        webServerThread = new Thread(webServer, "Web Server Host");
        webServerThread.setDaemon(true);
        webServerThread.start();
    }

    @Override
    public void unload() {
        this.webServer.disconnect();

        while (webServerThread.isAlive()) {
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

}
