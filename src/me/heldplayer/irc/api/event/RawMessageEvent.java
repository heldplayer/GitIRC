
package me.heldplayer.irc.api.event;

import me.heldplayer.irc.api.IRCMessage;

/**
 * Cancel me to stop default processing
 */
public class RawMessageEvent extends CancellableEvent {

    public final IRCMessage message;

    public RawMessageEvent(IRCMessage message) {
        this.message = message;
    }

}
