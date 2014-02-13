
package me.heldplayer.web.server;

import java.io.File;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IEntryPoint;
import me.heldplayer.irc.api.configuration.Configuration;
import me.heldplayer.irc.api.event.EventHandler;
import me.heldplayer.irc.api.event.user.UserMessageEvent;
import me.heldplayer.util.json.JSONObject;
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

        BotAPI.eventBus.registerEventHandler(this);
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

    @EventHandler
    public void commandEvent(UserMessageEvent event) {
        if (event.message.startsWith("!")) {
            String command = null;
            if (event.message.indexOf(" ") >= 0) {
                command = event.message.substring(1, event.message.indexOf(" "));
            }
            else {
                command = event.message.substring(1);
            }

            if (command.equalsIgnoreCase("json")) {
                if (event.message.indexOf(" ") < 0) {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG " + event.channel + " :" + event.user.getUsername() + ": Requires a parameter");
                    return;
                }
                try {
                    new JSONObject(event.message.substring(event.message.indexOf(" ") + 1));
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG " + event.channel + " :" + event.user.getUsername() + ": Parsing succeeded!");
                }
                catch (Throwable e) {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG " + event.channel + " :" + event.user.getUsername() + ": Error parsing JSON: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            else {
                BotAPI.serverConnection.addToSendQueue("PRIVMSG " + event.channel + " :" + event.user.getUsername() + ": Unknown command");
            }
        }
    }

}
