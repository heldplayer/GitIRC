package me.heldplayer.irc.git.internal;

public enum RequestMethod {

    NULL(false, false, "null"), GET(true, false, "get"), POST(true, true, "post"), HEAD(false, false, "head");

    public final boolean hasBody;
    private final String name;

    private RequestMethod(boolean hasSendBody, boolean hasReceiveBody, String name) {
        this.hasBody = hasSendBody;
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
