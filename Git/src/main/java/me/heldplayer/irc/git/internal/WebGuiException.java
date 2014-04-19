
package me.heldplayer.irc.git.internal;

import java.io.IOException;

public class WebGuiException extends IOException {

    private static final long serialVersionUID = -9081414239744769232L;

    public final ErrorResponse response;

    public WebGuiException(ErrorResponse response) {
        super();

        this.response = response;
    }

}
