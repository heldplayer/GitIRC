
package me.heldplayer.irc.base.java;

import java.util.ArrayList;

public class MethodPart extends NamedPart {

    public ArrayList<JavaPart> params;
    public boolean filled = false;

    public MethodPart(JavaPart parent, String name) {
        super(parent, name);
        params = new ArrayList<JavaPart>();
    }

    @Override
    public String toString() {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < this.params.size(); i++) {
            if (i > 0) {
                params.append(", ");
            }
            params.append(this.params.get(i));
        }
        return super.toString() + "(" + params.toString() + ")";
    }

}
