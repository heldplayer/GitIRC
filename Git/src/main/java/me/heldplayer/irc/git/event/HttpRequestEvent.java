
package me.heldplayer.irc.git.event;

import me.heldplayer.irc.api.event.Event;
import me.heldplayer.irc.git.RequestSource;
import me.heldplayer.irc.git.internal.WebResponse;
import me.heldplayer.irc.git.internal.ErrorResponse.ErrorType;

public class HttpRequestEvent extends Event {

    public final RequestSource source;
    public ErrorType error;
    public WebResponse response;

    public HttpRequestEvent(RequestSource source) {
        this.source = source;
    }

}
