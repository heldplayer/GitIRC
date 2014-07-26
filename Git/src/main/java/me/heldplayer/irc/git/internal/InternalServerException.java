package me.heldplayer.irc.git.internal;

public class InternalServerException extends RuntimeException {

    private static final long serialVersionUID = 1628113792829346255L;

    public InternalServerException(String message, Exception e) {
        super(message, e);
    }

    public InternalServerException(Exception e) {
        super(e);
    }

}
