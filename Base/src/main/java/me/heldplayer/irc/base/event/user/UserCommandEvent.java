
package me.heldplayer.irc.base.event.user;

import java.util.Arrays;

import me.heldplayer.irc.api.IRCUser;
import me.heldplayer.irc.api.event.CancellableEvent;

public class UserCommandEvent extends CancellableEvent {

    public final IRCUser user;
    public final String channel;
    public final String command;
    private String[] params;

    public UserCommandEvent(IRCUser user, String channel, String input) {
        this.user = user;
        this.channel = channel;
        String[] parts = input.split(" ");
        this.command = parts[0].toUpperCase();
        this.params = Arrays.copyOfRange(parts, 1, parts.length);
    }

    public UserCommandEvent(IRCUser user, String channel, String command, String[] params) {
        this.user = user;
        this.channel = channel;
        this.command = command;
        this.params = params;
    }

    public String[] getParams() {
        return Arrays.copyOf(this.params, this.params.length);
    }

    public void setHandled() {
        this.setCancelled(true);
    }

}
