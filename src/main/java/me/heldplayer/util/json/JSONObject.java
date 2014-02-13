
package me.heldplayer.util.json;

import java.util.HashMap;
import java.util.Map;

public class JSONObject {

    public Map<String, Object> values;

    public JSONObject(String input) {
        this(new JSONParser(input));
    }

    public JSONObject() {
        this.values = new HashMap<String, Object>();
    }

    JSONObject(JSONParser parser) {
        this();

        char c = parser.readNormalChar();

        if (c != '{') {
            throw new JSONException("Expected '{' but got '" + c + "' at " + parser.createErrorLocation());
        }

        while (true) {
            c = parser.readNormalChar();
            switch (c) {
            case 0:
                throw new JSONException("Expected '}' but got EOF at " + parser.createErrorLocation());
            case '}':
                return;
            default:
                parser.goBack();
            }

            Object val = parser.readValue();
            String key = null;
            if (val != null) {
                key = val.toString();
            }
            else {
                key = "null";
            }

            c = parser.readNormalChar();

            if (c != ':') {
                throw new JSONException("Expected ':' but got '" + c + "' at " + parser.createErrorLocation());
            }

            Object value = parser.readValue();

            this.values.put(key, value);

            c = parser.readNormalChar();
            switch (c) {
            case ';':
            case ',':
                if (parser.readChar() == '}') {
                    return;
                }
                parser.goBack();
            break;
            case '}':
                return;
            default:
                throw new JSONException("Expected ',', ';' or '}' but got '" + c + "' at " + parser.createErrorLocation());
            }
        }
    }

}
