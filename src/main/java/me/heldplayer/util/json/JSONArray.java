
package me.heldplayer.util.json;

import java.util.ArrayList;

public class JSONArray {

    public ArrayList<Object> values;

    public JSONArray(String input) {
        this(new JSONParser(input));
    }

    public JSONArray() {
        this.values = new ArrayList<Object>();
    }

    JSONArray(JSONParser parser) {
        this();

        char c = parser.readNormalChar();
        if (c != '[') {
            throw new JSONException("Expected '[' but got '" + c + "' at " + parser.createErrorLocation());
        }

        c = parser.readNormalChar();
        if (c != ']') {
            parser.goBack();

            while (true) {
                c = parser.readNormalChar();
                if (c == ',') {
                    parser.goBack();
                    this.values.add(JSONParser.NULL);
                }
                else {
                    parser.goBack();
                    this.values.add(parser.readValue());
                }

                c = parser.readNormalChar();
                switch (c) {
                case ',':
                    if (parser.readChar() == ']') {
                        return;
                    }
                    parser.goBack();
                break;
                case ']':
                    return;
                default:
                    throw new JSONException("Expected ',', or ']' but got '" + c + "' at " + parser.createErrorLocation());
                }
            }
        }
    }

}
