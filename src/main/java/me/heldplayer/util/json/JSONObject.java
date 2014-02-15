
package me.heldplayer.util.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JSONObject {

    public Map<String, Object> values;

    public JSONObject(String input) {
        this();

        this.load(new JSONParser(input));
    }

    public JSONObject(File file) throws FileNotFoundException {
        this();

        FileReader reader;
        this.load(new JSONParser(reader = new FileReader(file)));
        try {
            reader.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public JSONObject() {
        this.values = new HashMap<String, Object>();
    }

    JSONObject(JSONParser parser) {
        this();
        this.load(parser);
    }

    void load(JSONParser parser) {
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

    public Object getValue(String key) {
        return this.values.get(key);
    }

    public String getString(String key) {
        Object value = this.getValue(key);
        if (value != null) {
            if (value instanceof String) {
                return (String) value;
            }
            else if (value.equals(null)) {
                return "null";
            }

            throw new JSONException("Tried reading a String, got " + value.getClass().getSimpleName());
        }
        return null;
    }

    public boolean getBoolean(String key) {
        Object value = this.getValue(key);
        if (value != null) {
            if (value instanceof Boolean) {
                return ((Boolean) value).booleanValue();
            }
            else if (value.equals(null)) {
                return false;
            }

            throw new JSONException("Tried reading a Boolean, got " + value.getClass().getSimpleName());
        }
        return false;
    }

    public Number getNumber(String key) {
        Object value = this.getValue(key);
        if (value != null) {
            if (value instanceof Number) {
                return (Number) value;
            }
            else if (value.equals(null)) {
                return 0;
            }

            throw new JSONException("Tried reading a Number, got " + value.getClass().getSimpleName());
        }
        return 0;
    }

    public JSONObject getObject(String key) {
        Object value = this.getValue(key);
        if (value != null) {
            if (value instanceof JSONObject) {
                return (JSONObject) value;
            }
            else if (value.equals(null)) {
                return null;
            }

            throw new JSONException("Tried reading an Object, got " + value.getClass().getSimpleName());
        }
        return null;
    }

    public JSONArray getArray(String key) {
        Object value = this.getValue(key);
        if (value != null) {
            if (value instanceof JSONArray) {
                return (JSONArray) value;
            }
            else if (value.equals(null)) {
                return null;
            }

            throw new JSONException("Tried reading an Array, got " + value.getClass().getSimpleName());
        }
        return null;
    }

}
