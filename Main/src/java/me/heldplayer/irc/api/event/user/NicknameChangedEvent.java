
package me.heldplayer.irc.api.event.user;

import me.heldplayer.irc.api.IRCUser;

public class NicknameChangedEvent extends UserEvent {

    public final String oldNickname;

    public NicknameChangedEvent(IRCUser user, String oldNickname) {
        super(user);
        this.oldNickname = oldNickname;
    }

}
