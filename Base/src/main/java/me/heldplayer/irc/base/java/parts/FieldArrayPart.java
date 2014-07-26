package me.heldplayer.irc.base.java.parts;

public class FieldArrayPart extends NamedPart {

    public int index = 0;

    public FieldArrayPart(JavaPart parent, String name) {
        super(parent, name);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + this.index + "]";
    }

}
