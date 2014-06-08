
package me.heldplayer.irc.base.java.parts;

public abstract class JavaPart {

    public JavaPart parent;
    public JavaPart child;

    public JavaPart(JavaPart parent) {
        this.parent = parent;
        if (parent != null) {
            this.parent.child = this;
        }
    }

    public JavaPart() {
        this.parent = null;
    }

    @Override
    public abstract String toString();

    public JavaPart getRoot() {
        if (this.child != null) {
            return this.child.getRoot();
        }
        return this;
    }

}