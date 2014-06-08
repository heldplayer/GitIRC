
package me.heldplayer.irc.base.java;

public class StatementPart extends JavaPart {

    public String name;

    public StatementPart(String name) {
        super();
        this.name = name;
    }

    @Override
    public String toString() {
        if (this.child != null) {
            return this.name + " " + this.child.toString();
        }
        return this.name + " <EXPRESSION>";
    }

}
