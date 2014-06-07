
package me.heldplayer.irc.base.java;

public class NumberPart extends JavaPart {

    public Number value;

    public NumberPart(JavaPart parent, Number value) {
        super(parent);
        this.value = value;
    }

    public NumberPart(Number value) {
        super();
        this.value = value;
    }

    @Override
    public String toString() {
        if (this.parent != null) {
            return this.parent.toString() + "." + this.value;
        }
        return this.value.toString();
    }

}
