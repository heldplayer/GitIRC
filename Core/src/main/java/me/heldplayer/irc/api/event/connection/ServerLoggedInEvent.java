package me.heldplayer.irc.api.event.connection;

import me.heldplayer.irc.api.IServerConnection;

/**
 * Cancel me to stop default processing
 */
public class ServerLoggedInEvent extends ServerEvent {

    public ServerLoggedInEvent(IServerConnection connection) {
        super(connection);
    }

}
