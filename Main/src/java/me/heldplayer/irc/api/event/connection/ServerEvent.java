
package me.heldplayer.irc.api.event.connection;

import me.heldplayer.irc.api.IServerConnection;
import me.heldplayer.irc.api.event.Event;

public abstract class ServerEvent extends Event {

    public final IServerConnection connection;

    public ServerEvent(IServerConnection connection) {
        this.connection = connection;
    }

}
