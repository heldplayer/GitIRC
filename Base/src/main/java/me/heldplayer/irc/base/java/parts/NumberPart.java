
package me.heldplayer.irc.base.java.parts;

public class NumberPart extends JavaPart {

    public Number value;

    public NumberPart(Number value) {
        super();
        this.value = value;
    }

    @Override
    public String toString() {
        String suffix = "";
        if (value instanceof Long) {
            suffix = "L";
        }
        if (value instanceof Float) {
            suffix = "F";
        }
        if (value instanceof Double) {
            suffix = "D";
        }
        return this.value.toString() + suffix;
    }

}
