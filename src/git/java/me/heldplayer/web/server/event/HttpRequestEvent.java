
package me.heldplayer.web.server.event;

import me.heldplayer.irc.api.event.Event;
import me.heldplayer.web.server.RequestSource;
import me.heldplayer.web.server.internal.ErrorResponse.ErrorType;
import me.heldplayer.web.server.internal.WebResponse;

public class HttpRequestEvent extends Event {

    public final RequestSource source;
    public ErrorType error;
    public WebResponse response;

    public HttpRequestEvent(RequestSource source) {
        this.source = source;
    }

}
