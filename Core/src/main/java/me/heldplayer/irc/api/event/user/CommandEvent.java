package me.heldplayer.irc.api.event.user;

import me.heldplayer.irc.api.event.CancellableEvent;

import java.util.Arrays;

public class CommandEvent extends CancellableEvent {

    public String command;
    public String[] params;

    public CommandEvent(String input) {
        String[] parts = input.split(" ");
        if (parts[0].startsWith("/")) {
            parts[0] = parts[0].substring(1);
        }
        this.command = parts[0].toUpperCase();
        this.params = Arrays.copyOfRange(parts, 1, parts.length);
    }

    public CommandEvent(String command, String[] params) {
        this.command = command;
        this.params = params;
    }

    public void setHandled() {
        this.setCancelled(true);
    }

}
