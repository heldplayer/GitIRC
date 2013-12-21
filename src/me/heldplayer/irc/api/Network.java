
package me.heldplayer.irc.api;

public class Network {

    public String name;
    public int maxChannelModes = 1;
    public char[] channelTypes = new char[0];
    public char[][] prefixes = new char[0][0];
    public char[] channelModes = new char[0];

    public Network(String name) {
        this.name = name;
    }

}
