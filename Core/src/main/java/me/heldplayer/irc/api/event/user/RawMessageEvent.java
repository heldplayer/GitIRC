
package me.heldplayer.irc.api.event.user;

import me.heldplayer.irc.api.IRCMessage;
import me.heldplayer.irc.api.event.CancellableEvent;

/**
 * Cancel me to stop default processing
 */
public class RawMessageEvent extends CancellableEvent {

    public final IRCMessage message;

    public RawMessageEvent(IRCMessage message) {
        this.message = message;
    }

    public void setHandled() {
        this.setCancelled(true);
    }

}
