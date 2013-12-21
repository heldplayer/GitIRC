
package me.heldplayer.irc.api.event.connection;

import me.heldplayer.irc.api.IServerConnection;

/**
 * Cancel me to stop default processing
 */
public class ServerDisconnectedEvent extends ServerEvent {

    public ServerDisconnectedEvent(IServerConnection connection) {
        super(connection);
    }

}
