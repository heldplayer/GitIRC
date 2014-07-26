package me.heldplayer.irc.base.java;

public interface IMessageTarget {

    void sendMessage(String message);

    void sendMessage(String message, Object... params);

}
