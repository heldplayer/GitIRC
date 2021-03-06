package me.heldplayer.irc.api.event.user;

import me.heldplayer.irc.api.IRCMessage;
import me.heldplayer.irc.api.Network;
import me.heldplayer.irc.api.event.CancellableEvent;

/**
 * Cancel me to stop default processing
 */
public class RawMessageEvent extends CancellableEvent {

    public final IRCMessage message;
    public final Network network;

    public RawMessageEvent(Network network, IRCMessage message) {
        this.network = network;
        this.message = message;
    }

    public void setHandled() {
        this.setCancelled(true);
    }

}
