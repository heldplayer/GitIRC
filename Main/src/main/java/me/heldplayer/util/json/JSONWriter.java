
package me.heldplayer.util.json;

public class JSONWriter {

    private static String write(Object obj) {
        StringBuilder result = new StringBuilder();

        if (obj instanceof String) {
            result.append(write((String) obj));
        }
        else if (obj instanceof Boolean) {
            result.append(((Boolean) obj).toString());
        }
        else if (obj.equals(null)) {
            result.append("null");
        }
        else if (obj instanceof JSONObject) {
            result.append(write((JSONObject) obj));
        }
        else if (obj instanceof JSONArray) {
            result.append(write((JSONArray) obj));
        }
        else if (obj instanceof Double) {
            result.append(((Double) obj).doubleValue());
        }
        else if (obj instanceof Long) {
            result.append(((Long) obj).longValue());
        }
        else if (obj instanceof Integer) {
            result.append(((Integer) obj).intValue());
        }

        return result.toString();
    }

    public static String write(String string) {
        StringBuilder result = new StringBuilder();

        result.append('"');
        char[] chars = string.toCharArray();
        for (char character : chars) {
            if (",:{}[];=#\\\"'".indexOf(character) < 0) {
                switch (character) {
                case '\b':
                    result.append("\\b");
                break;
                case '\t':
                    result.append("\\t");
                break;
                case '\n':
                    result.append("\\n");
                break;
                case '\f':
                    result.append("\\f");
                break;
                case '\r':
                    result.append("\\r");
                break;
                default:
                    result.append(character);
                }
            }
            else {
                result.append('\\').append(character);
            }
        }
        result.append('"');

        return result.toString();
    }

    public static String write(JSONObject object) {
        StringBuilder result = new StringBuilder("{");

        for (String key : object.values.keySet()) {
            Object value = object.values.get(key);

            result.append(write(key)).append(':').append(write(value)).append(',');
        }
        result.delete(result.length() - 1, result.length());

        result.append("\"");

        return result.append("}").toString();
    }

    public static String write(JSONArray array) {
        StringBuilder result = new StringBuilder("[");

        for (Object value : array.values) {
            result.append(write(value)).append(',');
        }
        result.delete(result.length() - 1, result.length());

        return result.append("]").toString();
    }

}
