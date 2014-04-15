
package me.heldplayer.irc.api;

public interface IServerConnection {

    /**
     * Adds a raw IRC message to be sent to the server
     * 
     * @param command
     */
    void addToSendQueue(String command);

    /**
     * Makes the connection process its queue
     */
    void processQueue();

    Network getNetwork();

    /**
     * Gets the nickname of the bot on the server
     * 
     * @return
     */
    String getNickname();

    /**
     * Tells the server to change nickname
     * 
     * @param nickname
     */
    void setNickname(String nickname);

    /**
     * Tells the connection to disconnect from the server
     */
    void disconnect();

    /**
     * Tells the connection to disconnect from the server
     */
    void disconnect(String reason);

    /**
     * Returns whether the bot is connected to a server (the connection might
     * not have been fully initialized though)
     * 
     * @return
     */
    boolean isConnected();

    /**
     * Returns whether the bot is connected and running
     * 
     * @return
     */
    boolean isLoggedIn();

}
