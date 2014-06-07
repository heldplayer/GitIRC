
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
        this.value = parser.readPart(null);

        JavaPart next = parser.readPart(this.value);

        if (this.value instanceof NumberPart) {
            if (next != null) {
                throw new JavaException("Got '" + next.getClass().getSimpleName() + "' but expected nothing");
            }
        }

        while (next != null) {
            if (next instanceof FieldPart) {
                this.value = next;
                break;
            }
            this.value = next;
            next = parser.readPart(this.value);
        }
    }

    public void execute(IExpressionEvaluator evaluator) {
        evaluator.printString(this.value.toString() + " " + this.value.getClass());
    }

}
