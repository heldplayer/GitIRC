
package me.heldplayer.irc.base.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JavaExpression {

    public JavaPart value;

    public JavaExpression(String input) {
        this.load(new JavaReader(input));
    }

    public JavaExpression(InputStream in) {
        InputStreamReader reader;
        this.load(new JavaReader(reader = new InputStreamReader(in)));
        try {
            reader.close();
        }
        catch (IOException e) {
            throw new JavaException(e);
        }
    }

    JavaExpression(JavaReader parser) {
        this.load(parser);
    }

    void load(JavaReader parser) {
        this.value = parser.readExpression();
    }

}
