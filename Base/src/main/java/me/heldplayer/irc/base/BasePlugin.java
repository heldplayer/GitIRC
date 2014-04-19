
package me.heldplayer.irc.base;

import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IRCUser;
import me.heldplayer.irc.api.event.EventHandler;
import me.heldplayer.irc.api.event.user.RawMessageEvent;
import me.heldplayer.irc.api.plugin.Plugin;
import me.heldplayer.irc.base.event.user.UserCommandEvent;
import me.heldplayer.irc.base.event.user.UserMessageEvent;
import me.heldplayer.irc.base.event.user.UserNicknameChangedEvent;

public class BasePlugin extends Plugin {

    private static BasePlugin instance;

    public static Logger getLog() {
        return instance.getLogger();
    }

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {}

    @EventHandler
    public void onRawMessage(RawMessageEvent event) {
        if (event.message.command.equals("NICK")) {
            event.setHandled();
            String[] sender = event.message.prefix.split("!");
            IRCUser user = event.network.getUser(sender[0]);
            BotAPI.console.sendMessageToConsole("%s is now known as %s", user.getUsername(), event.message.params[0]);
            user.setUsername(event.message.params[0]);
            BotAPI.eventBus.postEvent(new UserNicknameChangedEvent(user, sender[0]));
        }
        else if (event.message.command.equals("PRIVMSG")) {
            event.setHandled();
            String[] sender = event.message.prefix.split("!");
            IRCUser user = event.network.getUser(sender[0]);
            BotAPI.console.sendMessageToConsole("[%s] <%s> %s", event.message.params[0], user.getUsername(), event.message.trailing);

            String prefix = BotAPI.configuration.getCommandPrefix();

            if (event.message.params[0].startsWith(prefix)) {
                UserCommandEvent commandEvent = new UserCommandEvent(user, event.message.params[0], event.message.trailing.substring(prefix.length()));
                if (BotAPI.eventBus.postEvent(commandEvent)) {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: Unknown command", event.message.params[0], user.getUsername());
                }
            }
            else {
                BotAPI.eventBus.postEvent(new UserMessageEvent(user, event.message.params[0], event.message.trailing));
            }
        }
    }

}
