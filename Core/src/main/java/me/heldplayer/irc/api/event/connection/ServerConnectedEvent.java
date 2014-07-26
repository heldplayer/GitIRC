package me.heldplayer.irc.api.event.connection;

import me.heldplayer.irc.api.IServerConnection;

/**
 * Cancel me to stop default processing
 */
public class ServerConnectedEvent extends ServerEvent {

    public ServerConnectedEvent(IServerConnection connection) {
        super(connection);
    }

}
