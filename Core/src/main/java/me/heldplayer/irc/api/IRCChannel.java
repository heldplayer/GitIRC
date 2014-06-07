
package me.heldplayer.irc.api;

import java.util.HashSet;

public class IRCChannel {

    private String channelName;
    public char[] channelModes = new char[0];
    private HashSet<IRCUser> users = new HashSet<IRCUser>();

    public IRCChannel(String username) {
        this.channelName = username;
    }

    public String getName() {
        return this.channelName;
    }

    public void setUsername(String username) {
        this.channelName = username;
    }

    public void addUser(IRCUser user) {
        this.users.add(user);
    }

    public void removeUser(IRCUser user) {
        this.users.remove(user);
    }

}
