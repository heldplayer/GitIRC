package me.heldplayer.irc.base.java;

public class JavaException extends RuntimeException {

    private static final long serialVersionUID = -8324459442544786192L;

    public JavaException() {
        super();
    }

    public JavaException(String message) {
        super(message);
    }

    public JavaException(String message, Object... args) {
        super(String.format(message, args));
    }

    public JavaException(Throwable cause) {
        super(cause);
    }

    public JavaException(Throwable cause, String message) {
        super(message, cause);
    }

    public JavaException(Throwable cause, String message, Object... args) {
        super(String.format(message, args), cause);
    }

}
