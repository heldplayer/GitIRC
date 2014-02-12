
package me.heldplayer.web.server;

public class RequestFlags {

    public Method method = Method.GET;

    public enum Method {
        NULL(false, "null"),
        GET(true, "get"),
        POST(true, "post"),
        HEAD(false, "head");

        public final boolean hasBody;
        private final String name;

        private Method(boolean hasBody, String name) {
            this.hasBody = hasBody;
            this.name = name;
        }

        public static Method fromString(String string) {
            for (Method method : Method.values()) {
                if (method.name.equalsIgnoreCase(string)) {
                    return method;
                }
            }
            return NULL;
        }
    }

}
