package me.heldplayer.irc.api.event.user;

import me.heldplayer.irc.api.event.Event;

public class SelfNicknameChangedEvent extends Event {

    public final String newNickname;
    public final String oldNickname;

    public SelfNicknameChangedEvent(String newNickname, String oldNickname) {
        this.newNickname = newNickname;
        this.oldNickname = oldNickname;
    }

}
