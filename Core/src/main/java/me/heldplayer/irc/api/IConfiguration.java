
package me.heldplayer.irc.api;

public interface IConfiguration {

    String getServerIp();

    int getServerPort();

    String getNickname();

    String getBindHost();

    String getLogFile();

    String getCommandPrefix();

}
