package me.heldplayer.web.server.internal;

public enum RequestMethod {
    NULL(false, "null"),
    GET(true, "get"),
    POST(true, "post"),
    HEAD(false, "head");

    public final boolean hasBody;
    private final String name;

    private RequestMethod(boolean hasBody, String name) {
        this.hasBody = hasBody;
        this.name = name;
    }

    public static RequestMethod fromString(String string) {
        for (RequestMethod method : RequestMethod.values()) {
            if (method.name.equalsIgnoreCase(string)) {
                return method;
            }
        }
        return NULL;
    }
}