package me.heldplayer.irc.base.event.user;

import me.heldplayer.irc.api.IRCUser;

public class UserNicknameChangedEvent extends UserEvent {

    public final String oldNickname;

    public UserNicknameChangedEvent(IRCUser user, String oldNickname) {
        super(user);
        this.oldNickname = oldNickname;
    }

}
