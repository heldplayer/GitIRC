
package me.heldplayer.irc.api;

public class Network {

    public String name;
    public int maxChannelModes = 1;
    public char[][] prefixes = new char[0][0];
    public char[] availableChannelTypes = new char[0];
    public char[] availableChannelModes = new char[0];

    public Network(String name) {
        this.name = name;
    }

}
