
package me.heldplayer.irc.api;

public class IRCUser {

    private String username;
    public char[] userModes = new char[0];

    public IRCUser(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    protected void setUsername(String username) {
        this.username = username;
    }

}
