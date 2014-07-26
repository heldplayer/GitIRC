package me.heldplayer.util.json;

import java.util.ArrayList;

public class JSONArray {

    public ArrayList<Object> values;

    public JSONArray(String input) {
        this(new JSONParser(input));
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
                } else {
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

    public JSONArray() {
        this.values = new ArrayList<Object>();
    }

    public int size() {
        return this.values.size();
    }

    public String getString(int index) {
        Object value = this.getValue(index);
        if (value != null) {
            if (value instanceof String) {
                return (String) value;
            } else if (value.equals(null)) {
                return "null";
            }

            throw new JSONException("Tried reading a String, got " + value.getClass().getSimpleName());
        }
        return null;
    }

    public Object getValue(int index) {
        return this.values.get(index);
    }

    public boolean getBoolean(int index) {
        Object value = this.getValue(index);
        if (value != null) {
            if (value instanceof Boolean) {
                return ((Boolean) value).booleanValue();
            } else if (value.equals(null)) {
                return false;
            }

            throw new JSONException("Tried reading a Boolean, got " + value.getClass().getSimpleName());
        }
        return false;
    }

    public Number getNumber(int index) {
        Object value = this.getValue(index);
        if (value != null) {
            if (value instanceof Number) {
                return (Number) value;
            } else if (value.equals(null)) {
                return 0;
            }

            throw new JSONException("Tried reading a Number, got " + value.getClass().getSimpleName());
        }
        return 0;
    }

    public JSONObject getObject(int index) {
        Object value = this.getValue(index);
        if (value != null) {
            if (value instanceof JSONObject) {
                return (JSONObject) value;
            } else if (value.equals(null)) {
                return null;
            }

            throw new JSONException("Tried reading an Object, got " + value.getClass().getSimpleName());
        }
        return null;
    }

    public JSONArray getArray(int index) {
        Object value = this.getValue(index);
        if (value != null) {
            if (value instanceof JSONArray) {
                return (JSONArray) value;
            } else if (value.equals(null)) {
                return null;
            }

            throw new JSONException("Tried reading an Array, got " + value.getClass().getSimpleName());
        }
        return null;
    }

}
