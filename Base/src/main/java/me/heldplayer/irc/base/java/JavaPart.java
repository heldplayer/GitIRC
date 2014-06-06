
package me.heldplayer.irc.base.java;

public abstract class JavaPart {

    public JavaPart parent;

    public JavaPart(JavaPart parent) {
        this.parent = parent;
    }

    public JavaPart() {
        this.parent = null;
    }

    public abstract String toString();

}
