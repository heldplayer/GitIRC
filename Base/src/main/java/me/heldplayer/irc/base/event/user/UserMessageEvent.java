package me.heldplayer.irc.base.event.user;

import me.heldplayer.irc.api.IRCUser;

public class UserMessageEvent extends UserEvent {

    public final String message;
    public final String channel;

    public UserMessageEvent(IRCUser user, String channel, String message) {
        super(user);
        this.channel = channel;
        this.message = message;
    }

}
