
package me.heldplayer.irc.base.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JavaObject {

    public JavaPart value;

    public JavaObject(String input) {
        this.load(new JavaReader(input));
    }

    public JavaObject(InputStream in) {
        InputStreamReader reader;
        this.load(new JavaReader(reader = new InputStreamReader(in)));
        try {
            reader.close();
        }
        catch (IOException e) {
            throw new JavaException(e);
        }
    }

    JavaObject(JavaReader parser) {
        this.load(parser);
    }

    void load(JavaReader parser) {
        this.value = parser.readIdentifierPart(null);
    }

}
