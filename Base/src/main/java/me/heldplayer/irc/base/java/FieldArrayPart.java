
package me.heldplayer.irc.base.java;

public class FieldArrayPart extends FieldPart {

    public int index = 0;

    public FieldArrayPart(JavaPart parent, String name) {
        super(parent, name);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + this.index + "]";
    }

}
