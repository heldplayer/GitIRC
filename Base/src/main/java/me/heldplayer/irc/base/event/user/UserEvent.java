package me.heldplayer.irc.base.event.user;

import me.heldplayer.irc.api.IRCUser;
import me.heldplayer.irc.api.event.Event;

public class UserEvent extends Event {

    public final IRCUser user;

    public UserEvent(IRCUser user) {
        this.user = user;
    }

}
