
package me.heldplayer.irc.api.event.user;

import java.util.Arrays;

import me.heldplayer.irc.api.IRCUser;

public class UserCommandEvent extends UserEvent {

    public final String channel;
    public final String command;
    private String[] params;

    public UserCommandEvent(IRCUser user, String channel, String input) {
        super(user);
        this.channel = channel;
        String[] parts = input.split(" ");
        this.command = parts[0].toUpperCase();
        this.params = Arrays.copyOfRange(parts, 1, parts.length - 1);
    }

    public UserCommandEvent(IRCUser user, String channel, String command, String[] params) {
        super(user);
        this.channel = channel;
        this.command = command;
        this.params = params;
    }

    public String[] getParams() {
        return Arrays.copyOf(this.params, this.params.length);
    }

}
