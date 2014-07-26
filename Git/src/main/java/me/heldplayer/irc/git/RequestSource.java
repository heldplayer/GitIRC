package me.heldplayer.irc.git;

import me.heldplayer.irc.git.internal.RequestMethod;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

public class RequestSource {

    public final InetAddress address;
    public final Map<String, String> headers;
    public final RequestMethod method;
    public String path;
    public String body;

    public RequestSource(InetAddress address, RequestMethod method, Map<String, String> headers, String path) {
        this.address = address;
        this.method = method;
        this.headers = Collections.unmodifiableMap(headers);
        this.path = path;
    }

}
