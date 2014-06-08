
package me.heldplayer.irc.base.java.parts;

public class StringPart extends JavaPart {

    public String value;

    public StringPart(String name) {
        super();
        this.value = name;
    }

    @Override
    public String toString() {
        return "\"" + this.value + "\"";
    }

}
