
package me.heldplayer.irc.base;

import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IRCUser;
import me.heldplayer.irc.api.event.EventHandler;
import me.heldplayer.irc.api.event.user.RawMessageEvent;
import me.heldplayer.irc.api.plugin.Plugin;
import me.heldplayer.irc.base.event.user.UserCommandEvent;
import me.heldplayer.irc.base.event.user.UserMessageEvent;
import me.heldplayer.irc.base.event.user.UserNicknameChangedEvent;
import me.heldplayer.irc.base.java.ISandboxDelegate;
import me.heldplayer.irc.base.java.JavaReader;
import me.heldplayer.irc.base.java.SandboxManager;
import me.heldplayer.util.json.JSONObject;

public class BasePlugin extends Plugin {

    private static BasePlugin instance;

    public static Logger getLog() {
        return instance.getLogger();
    }

    @Override
    public void onEnable() {
        instance = this;

        BotAPI.eventBus.registerEventHandler(this);
    }

    @Override
    public void onDisable() {
        SandboxManager.resetAll();
    }

    @EventHandler
    public void onRawMessage(RawMessageEvent event) {
        if (event.message.command.equals("NICK")) {
            event.setHandled();
            String[] sender = event.message.prefix.split("!");
            IRCUser user = event.network.getUser(sender[0]);
            BotAPI.console.sendMessageToConsole("%s is now known as %s", user.getUsername(), event.message.trailing);
            event.network.setUsername(user, event.message.trailing);
            BotAPI.eventBus.postEvent(new UserNicknameChangedEvent(user, sender[0]));
        }
        else if (event.message.command.equals("PRIVMSG")) {
            event.setHandled();
            String[] sender = event.message.prefix.split("!");
            IRCUser user = event.network.getUser(sender[0]);
            BotAPI.console.sendMessageToConsole("[%s] <%s> %s", event.message.params[0], user.getUsername(), event.message.trailing);

            String prefix = BotAPI.configuration.getCommandPrefix();

            if (event.message.trailing.startsWith(prefix)) {
                UserCommandEvent commandEvent = new UserCommandEvent(user, event.message.params[0], event.message.trailing.substring(prefix.length()));

                try {
                    BotAPI.eventBus.postEvent(commandEvent);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s: %s", commandEvent.channel, commandEvent.user.getUsername(), e.getClass().getSimpleName(), e.getMessage());
                }
            }
            else {
                BotAPI.eventBus.postEvent(new UserMessageEvent(user, event.message.params[0], event.message.trailing));
            }
        }
    }

    @EventHandler
    public void onUserCommand(UserCommandEvent event) throws Throwable {
        if (event.command.equals("JSON")) {
            event.setHandled();
            String[] params = event.getParams();

            if (params.length == 0) {
                BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), "Usage: json parse/url");
            }
            else if (params.length == 1) {
                if (params[0].equalsIgnoreCase("parse")) {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), "Usage: minecraft parse [JSON]");
                }
                else if (params[0].equalsIgnoreCase("url")) {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), "Usage: minecraft url [URL]");
                }
                else {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), "Usage: json parse/url");
                }
            }
            else {
                if (params[0].equalsIgnoreCase("parse")) {
                    String[] temp = Arrays.copyOfRange(params, 1, params.length);
                    String json = temp[0];
                    for (int i = 1; i < temp.length; i++) {
                        if (temp[i] != null) {
                            json += " " + temp[i];
                        }
                    }
                    try {
                        new JSONObject(json);

                        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), "Parsing succeeded");
                    }
                    catch (Throwable e) {
                        e.printStackTrace();
                        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), "Parsing failed");
                        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), e.getClass().getSimpleName(), e.getMessage());
                    }
                }
                else if (params[0].equalsIgnoreCase("url")) {
                    String[] temp = Arrays.copyOfRange(params, 1, params.length);
                    String url = temp[0];
                    for (int i = 1; i < temp.length; i++) {
                        if (temp[i] != null) {
                            url += " " + temp[i];
                        }
                    }
                    try {
                        new JSONObject(new URL(url).openStream());

                        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), "Parsing succeeded");
                    }
                    catch (Throwable e) {
                        e.printStackTrace();
                        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), "Parsing failed");
                        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), e.getClass().getSimpleName(), e.getMessage());
                    }
                }
                else {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), "Usage: minecraft uuid/name");
                }
            }
        }
        else if (event.command.equals("UPTIME")) {
            long totalTime = System.currentTimeMillis() - BotAPI.startTime;
            long time = totalTime / 1000L;
            long seconds = time % 60;
            long minutes = ((time - seconds) / 60L) % 60;
            long hours = ((time - seconds - minutes * 60L) / 3600L) % 24;
            long days = ((time - seconds - minutes * 60L - hours * 1440L) / 86400L) % 7;
            long weeks = ((time - seconds - minutes * 60L - hours * 1440L - days * 10080L) / 604800L);

            StringBuilder result = new StringBuilder();
            boolean flag = false;
            if (weeks > 0) {
                flag = true;
                result.append(weeks).append(" weeks, ");
            }
            if (flag) {
                result.append(days).append(" days, ");
            }
            else if (days > 0) {
                flag = true;
                result.append(days).append(" days, ");
            }
            if (flag) {
                result.append(hours).append(" hours, ");
            }
            else if (hours > 0) {
                flag = true;
                result.append(hours).append(" hours, ");
            }
            if (flag) {
                result.append(minutes).append(" minutes, ");
            }
            else if (minutes > 0) {
                flag = true;
                result.append(minutes).append(" minutes, ");
            }
            result.append(seconds).append(" seconds");
            BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: Uptime: %s", event.channel, event.user.getUsername(), result.toString());
        }
        else if (event.command.equals("SANDBOX") || event.command.equals("SB")) {

            try {
                event.setHandled();
                String[] params = event.getParams();

                if (params.length == 0) {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel, event.user.getUsername(), "Usage: sandbox list/create/eval <expression>");
                }
                else if (params.length == 1) {
                    if (params[0].equalsIgnoreCase("list")) {
                        Set<IRCUser> users = SandboxManager.sandboxes.keySet();
                        StringBuilder sandboxes = null;
                        for (IRCUser user : users) {
                            if (sandboxes == null) {
                                sandboxes = new StringBuilder();
                            }
                            else {
                                sandboxes.append(", ");
                            }
                            sandboxes.append(user.getUsername());
                        }

                        if (sandboxes == null) {
                            sandboxes = new StringBuilder("None");
                        }
                        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: Active sandboxes: %s", event.channel, event.user.getUsername(), sandboxes);
                    }
                    else if (params[0].equalsIgnoreCase("create")) {
                        try {
                            SandboxManager.createSandbox(event.user);
                            BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: Created new sandbox", event.channel, event.user.getUsername());
                        }
                        catch (Throwable e) {
                            BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: Failed creating sandbox: %s: %s", event.channel, event.user.getUsername(), e.getClass().getSimpleName(), e.getMessage());
                        }
                    }
                }
                else {
                    if (params[0].equalsIgnoreCase("eval")) {
                        String[] temp = Arrays.copyOfRange(params, 1, params.length);
                        String message = temp[0];
                        for (int i = 1; i < temp.length; i++) {
                            if (temp[i] != null) {
                                message += " " + temp[i];
                            }
                        }
                        System.out.println(message);
                        ISandboxDelegate sandbox = SandboxManager.getSandbox(event.user);
                        if (sandbox == null) {
                            BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: You have no running sandbox", event.channel, event.user.getUsername());
                        }
                        else {
                            sandbox.addCommand(new JavaReader(message).readNumber().toString());
                        }
                    }
                }
            }
            catch (Throwable e) {
                BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: Error: %s: %s", event.channel, event.user.getUsername(), e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

}
