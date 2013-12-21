
package me.heldplayer.irc.api.event.chat;

import me.heldplayer.irc.api.event.CancellableEvent;

public class CommandEvent extends CancellableEvent {

    public String command;
    public String[] params;

    public CommandEvent(String input) {
        String[] parts = input.split(" ");
        if (parts[0].startsWith("/")) {
            parts[0] = parts[0].substring(1);
        }
        this.command = parts[0];
        this.params = new String[parts.length - 1];
        System.arraycopy(parts, 1, this.params, 0, params.length);
    }

    public CommandEvent(String command, String[] params) {
        this.command = command;
        this.params = params;
    }

    public void setHandled() {
        this.setCancelled(true);
    }

}
