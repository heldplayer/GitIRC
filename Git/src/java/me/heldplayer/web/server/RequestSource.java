
package me.heldplayer.web.server;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

import me.heldplayer.web.server.internal.RequestMethod;

public class RequestSource {

    public final InetAddress address;
    public String path;
    public final Map<String, String> headers;
    public final RequestMethod method;

    public String body;

    public RequestSource(InetAddress address, RequestMethod method, Map<String, String> headers, String path) {
        this.address = address;
        this.method = method;
        this.headers = Collections.unmodifiableMap(headers);
        this.path = path;
    }

}
