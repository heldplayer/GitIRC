package me.heldplayer.util.json;

public class JSONWriter {

    public static String write(JSONObject object) {
        return write("  ", object);
    }

    public static String write(JSONArray array) {
        return write("  ", array);
    }

    private static String write(String prefix, Object obj) {
        StringBuilder result = new StringBuilder();

        if (obj instanceof String) {
            result.append(JSONWriter.write((String) obj));
        } else if (obj instanceof Boolean) {
            result.append(((Boolean) obj).toString());
        } else if (obj.equals(null)) {
            result.append("null");
        } else if (obj instanceof JSONObject) {
            result.append(JSONWriter.write(prefix, (JSONObject) obj));
        } else if (obj instanceof JSONArray) {
            result.append(JSONWriter.write(prefix, (JSONArray) obj));
        } else if (obj instanceof Double) {
            result.append(((Double) obj).doubleValue());
        } else if (obj instanceof Long) {
            result.append(((Long) obj).longValue());
        } else if (obj instanceof Integer) {
            result.append(((Integer) obj).intValue());
        }

        return result.toString();
    }

    private static String write(String string) {
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
            } else {
                result.append('\\').append(character);
            }
        }
        result.append('"');

        return result.toString();
    }

    private static String write(String prefix, JSONObject object) {
        StringBuilder result = new StringBuilder("{\n");

        for (String key : object.values.keySet()) {
            Object value = object.values.get(key);

            result.append(prefix).append(JSONWriter.write(key)).append(':').append(JSONWriter.write(prefix + "  ", value)).append(',').append('\n');
        }
        result.delete(result.length() - 2, result.length());
        result.append('\n').append(prefix.substring(2));

        return result.append("}").toString();
    }

    private static String write(String prefix, JSONArray array) {
        StringBuilder result = new StringBuilder("[\n");

        for (Object value : array.values) {
            result.append(prefix).append(JSONWriter.write(prefix + "  ", value)).append(',').append('\n');
        }
        result.delete(result.length() - 2, result.length());
        result.append('\n').append(prefix.substring(2));

        return result.append("]").toString();
    }

}
