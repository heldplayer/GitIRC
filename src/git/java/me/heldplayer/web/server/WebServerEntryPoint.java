
package me.heldplayer.web.server;

import java.io.File;
import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IEntryPoint;
import me.heldplayer.irc.api.configuration.Configuration;
import me.heldplayer.irc.api.event.EventHandler;
import me.heldplayer.irc.api.event.user.UserMessageEvent;
import me.heldplayer.util.json.JSONObject;
import me.heldplayer.web.server.event.AccessManagerInitEvent;
import me.heldplayer.web.server.internal.RunnableWebserver;
import me.heldplayer.web.server.internal.security.AccessManager;
import me.heldplayer.web.server.internal.security.require.AllowAll;
import me.heldplayer.web.server.internal.security.require.DenyAll;
import me.heldplayer.web.server.internal.security.require.RequireAll;
import me.heldplayer.web.server.internal.security.require.RequireIp;
import me.heldplayer.web.server.internal.security.require.RequireNone;
import me.heldplayer.web.server.internal.security.require.RequireOne;

public class WebServerEntryPoint implements IEntryPoint {

    public static Configuration config;

    public static File webDirectory;

    private RunnableWebserver webServer;
    private Thread webServerThread;

    public static final Logger log = Logger.getLogger("Web");

    @Override
    public void load() {
        WebServerEntryPoint.config = new Configuration(new File("." + File.separator + "webserver.cfg"));
        WebServerEntryPoint.config.load();

        String directory = WebServerEntryPoint.config.getString("web-directory");
        File file = WebServerEntryPoint.webDirectory = new File(directory);
        if (!file.exists()) {
            file.mkdirs();
        }
        else if (!file.isDirectory()) {
            throw new RuntimeException("Web directory '" + directory + "' is not a directory");
        }

        BotAPI.eventBus.registerEventHandler(this);

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

        AccessManager.cleanupRules();
    }

    @EventHandler
    public void onAccessManagerInit(AccessManagerInitEvent event) {
        AccessManager.registerRule("allowAll", AllowAll.class);
        AccessManager.registerRule("denyAll", DenyAll.class);
        AccessManager.registerRule("requireAll", RequireAll.class);
        AccessManager.registerRule("requireOne", RequireOne.class);
        AccessManager.registerRule("requireNone", RequireNone.class);
        AccessManager.registerRule("requireIp", RequireIp.class);
    }

    @EventHandler
    public void commandEvent(UserMessageEvent event) {
        if (event.message.startsWith("&")) {
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
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG " + event.channel + " :" + event.user.getUsername() + ": Error parsing JSON: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            else {
                BotAPI.serverConnection.addToSendQueue("PRIVMSG " + event.channel + " :" + event.user.getUsername() + ": Unknown command");
            }
        }
    }
}
