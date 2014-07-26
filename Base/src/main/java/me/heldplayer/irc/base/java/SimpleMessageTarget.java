package me.heldplayer.irc.base.java;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IRCChannel;
import me.heldplayer.irc.api.IRCUser;

public class SimpleMessageTarget implements IMessageTarget {

    private IRCUser user;
    private IRCChannel channel;

    protected SimpleMessageTarget(IRCUser user, IRCChannel channel) {
        this.user = user;
        this.channel = channel;
    }

    @Override
    public void sendMessage(String message) {
        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", channel.getName(), this.user.getUsername(), message);
    }

    @Override
    public void sendMessage(String message, Object... params) {
        this.sendMessage(String.format(message, params));
    }

}
