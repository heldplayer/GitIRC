
package me.heldplayer.web.server;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IEntryPoint;
import me.heldplayer.irc.api.configuration.Configuration;
import me.heldplayer.irc.api.event.EventHandler;
import me.heldplayer.irc.api.event.user.UserMessageEvent;
import me.heldplayer.util.json.JSONObject;
import me.heldplayer.web.server.event.AccessManagerInitEvent;
import me.heldplayer.web.server.event.HttpRequestEvent;
import me.heldplayer.web.server.internal.EmptyResponse;
import me.heldplayer.web.server.internal.ErrorResponse.ErrorType;
import me.heldplayer.web.server.internal.QueryString;
import me.heldplayer.web.server.internal.RunnableWebserver;
import me.heldplayer.web.server.internal.security.AccessManager;
import me.heldplayer.web.server.internal.security.require.AllowFrom;
import me.heldplayer.web.server.internal.security.require.BasicAuth;
import me.heldplayer.web.server.internal.security.require.DenyFrom;
import me.heldplayer.web.server.internal.security.require.IpRangeRule;
import me.heldplayer.web.server.internal.security.require.RequireAll;
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
    public void onHttpRequest(HttpRequestEvent event) {
        if (event.source.path.equals("/github")) {
            QueryString query = new QueryString(event.source.body);
            if (query.values.containsKey("payload")) {
                try {
                    System.out.println("payload=" + query.values.get("payload"));
                    JSONObject obj = new JSONObject(query.values.get("payload"));
                }
                catch (Throwable e) {
                    e.printStackTrace();
                    event.error = ErrorType.InternalServerError;
                }

                try {
                    event.response = new EmptyResponse();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    event.error = ErrorType.InternalServerError;
                }
            }
            else {
                event.error = ErrorType.BadRequest;
            }
        }
    }

    @EventHandler
    public void onAccessManagerInit(AccessManagerInitEvent event) {
        AccessManager.registerRule("allowFrom", AllowFrom.class);
        AccessManager.registerRule("denyFrom", DenyFrom.class);
        AccessManager.registerRule("requireAll", RequireAll.class);
        AccessManager.registerRule("requireOne", RequireOne.class);
        AccessManager.registerRule("requireNone", RequireNone.class);
        AccessManager.registerRule("ipRange", IpRangeRule.class);
        AccessManager.registerRule("basicAuth", BasicAuth.class);
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
