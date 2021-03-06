package me.heldplayer.irc.git;

import me.heldplayer.irc.IRCBotLauncher;
import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.configuration.Configuration;
import me.heldplayer.irc.api.event.EventHandler;
import me.heldplayer.irc.api.event.user.CommandEvent;
import me.heldplayer.irc.api.plugin.Plugin;
import me.heldplayer.irc.api.plugin.PluginException;
import me.heldplayer.irc.git.event.AccessManagerInitEvent;
import me.heldplayer.irc.git.event.HttpRequestEvent;
import me.heldplayer.irc.git.internal.EmptyResponse;
import me.heldplayer.irc.git.internal.ErrorResponse.ErrorType;
import me.heldplayer.irc.git.internal.QueryString;
import me.heldplayer.irc.git.internal.RunnableWebserver;
import me.heldplayer.irc.git.internal.security.AccessManager;
import me.heldplayer.irc.git.internal.security.rules.*;
import me.heldplayer.irc.util.Format;
import me.heldplayer.irc.util.Util;
import me.heldplayer.util.json.JSONArray;
import me.heldplayer.util.json.JSONObject;
import me.heldplayer.util.json.JSONWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitPlugin extends Plugin {

    public static Configuration config;

    public static File webDirectory;
    private static GitPlugin instance;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private RunnableWebserver webServer;
    private Thread webServerThread;
    private String channel;

    @Override
    public void onEnable() {
        GitPlugin.instance = this;

        GitPlugin.config = new Configuration(new File(IRCBotLauncher.rootDirectory, "webserver.cfg"));
        GitPlugin.config.load();

        String directory = GitPlugin.config.getString("web-directory");
        File file = GitPlugin.webDirectory = new File(IRCBotLauncher.rootDirectory, directory);
        if (!file.exists()) {
            file.mkdirs();
        } else if (!file.isDirectory()) {
            throw new RuntimeException("Web directory '" + directory + "' is not a directory");
        }

        BotAPI.eventBus.registerEventHandler(this);

        String bindhost = GitPlugin.config.getString("bind-host");
        int port = GitPlugin.config.getInt("port");

        this.webServer = new RunnableWebserver(port, bindhost);
        this.webServerThread = new Thread(this.webServer, "Web Server Host");
        this.webServerThread.setDaemon(true);
        this.webServerThread.start();
    }

    @Override
    public void onDisable() {
        this.webServer.disconnect();

        while (this.webServerThread.isAlive()) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
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
            if (query.values.containsKey("payload") && event.source.headers.containsKey("X-GitHub-Event")) {
                try {
                    String eventType = event.source.headers.get("X-GitHub-Event");
                    JSONObject obj = new JSONObject(query.values.get("payload"));

                    try {
                        JSONObject temp = new JSONObject();
                        temp.values.put("event", eventType);
                        temp.values.put("payload", obj);

                        saveReport(JSONWriter.write(temp));
                    } catch (Throwable e) {
                        getLog().log(Level.WARNING, "Failed saving github report", e);
                    }

                    if (eventType.equals("ping")) {
                        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :Zen: %s", this.channel, obj.getString("zen"));
                    } else if (eventType.equals("push")) {
                        JSONArray commits = obj.getArray("commits");
                        String repository = obj.getObject("repository").getString("name");
                        String ref = obj.getString("ref").substring(11);

                        for (int i = 0; i < commits.size(); i++) {
                            JSONObject commit = commits.getObject(i);
                            String message = commit.getString("message").replaceAll("\n", " ");
                            message = message.replaceAll("\r", "");

                            String url = commit.getString("url");
                            try {
                                url = "http://git.io/" + Util.createGitIO(url);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }

                            String output = Format.BOLD + "%s" + Format.RESET + "/%s - " + Format.PURPLE + "%s" + Format.RESET + ": %s +" + Format.DARK_GREEN + "%s" + Format.RESET + " ~" + Format.ORANGE + "%s" + Format.RESET + " -" + Format.RED + "%s" + Format.RESET + " %s";
                            output = String.format(output, repository, ref, commit.getObject("author").getString("name"), message, commit.getArray("added").size(), commit.getArray("modified").size(), commit.getArray("removed").size(), url);

                            BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s", this.channel, output);
                        }
                    } else if (eventType.equals("issues")) {
                        String repository = obj.getObject("repository").getString("name");

                        JSONObject issue = obj.getObject("issue");

                        String issuer = issue.getObject("user").getString("login");

                        String action = obj.getString("action");
                        String actionString = "";

                        if (action.equals("opened")) {
                            actionString = "openend a new issue";
                        } else {
                            actionString = action + " an issue";
                        }

                        String url = Util.createGitIO(issue.getString("html_url"));

                        String output = Format.BOLD + "%s" + Format.RESET + " - " + Format.PURPLE + "%s" + Format.RESET + " %s - http://git.io/%s";
                        output = String.format(output, repository, issuer, actionString, url);

                        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s", this.channel, output);
                    } else {
                        String output = "Received an unknown event from github, please contact heldplayer";
                        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s", this.channel, output);

                        System.out.println("event=" + eventType);
                        System.out.println("File saved as report-" + dateFormat.format(new Date(System.currentTimeMillis())) + ".txt");

                        throw new RuntimeException("Unknown event");
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    event.error = ErrorType.InternalServerError;
                }

                try {
                    event.response = new EmptyResponse();
                } catch (IOException e) {
                    e.printStackTrace();
                    event.error = ErrorType.InternalServerError;
                }
            } else {
                event.error = ErrorType.BadRequest;
            }
        }
    }

    private static void saveReport(String content) throws IOException {
        File reportsDir = new File(IRCBotLauncher.rootDirectory, "git" + File.separator + "reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        if (!reportsDir.isDirectory()) {
            throw new PluginException("'" + reportsDir.getPath() + "' is not a directory file");
        }

        File report = new File(reportsDir, "report-" + dateFormat.format(new Date(System.currentTimeMillis())) + ".txt");

        if (!report.exists()) {
            report.createNewFile();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(report));

        writer.append(content);

        writer.close();
    }

    public static Logger getLog() {
        return GitPlugin.instance.getLogger();
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
    public void onCommand(CommandEvent event) {
        if (event.command.equals("GIT")) {
            if (event.params.length == 1) {
                this.channel = event.params[0];
            } else {
                BotAPI.console.log(Level.WARNING, "Expected 1 parameter for command /git");
            }
            event.setHandled();
        }
    }

}
