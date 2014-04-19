
package me.heldplayer.irc.api;

import java.util.TreeMap;

public class Network {

    public String name;
    public int maxChannelModes = 1;
    public char[][] prefixes = new char[0][0];
    public char[] availableChannelTypes = new char[0];
    public char[] availableChannelModes = new char[0];

    private TreeMap<String, IRCUser> users = new TreeMap<String, IRCUser>();

    public Network(String name) {
        this.name = name;
    }

    public IRCUser getUser(String name) {
        if (this.users.containsKey(name)) {
            return this.users.get(name);
        }

        IRCUser user = new IRCUser(name);
        this.users.put(name, user);
        return user;
    }

    public void userDisconnected(String name) {
        this.users.remove(name);
    }

}
