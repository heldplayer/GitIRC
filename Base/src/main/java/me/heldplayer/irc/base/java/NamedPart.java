
package me.heldplayer.irc.base.java;

public class NamedPart extends JavaPart {

    public String name;

    public NamedPart(JavaPart parent, String name) {
        super(parent);
        this.name = name;
    }

    public NamedPart(String name) {
        super();
        this.name = name;
    }

    @Override
    public String toString() {
        if (this.parent != null) {
            return this.parent.toString() + "." + this.name;
        }
        return this.name;
    }

}
