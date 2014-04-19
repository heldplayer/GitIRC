
package me.heldplayer.web.server.internal;

public class InternalServerException extends RuntimeException {

    public InternalServerException(String message, Exception e) {
        super(message, e);
    }

    public InternalServerException(Exception e) {
        super(e);
    }

    private static final long serialVersionUID = 1628113792829346255L;

}
