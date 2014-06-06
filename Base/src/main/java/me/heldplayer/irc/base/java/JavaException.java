
package me.heldplayer.irc.base.java;

public class JavaException extends RuntimeException {

    private static final long serialVersionUID = -8324459442544786192L;

    public JavaException() {
        super();
    }

    public JavaException(String message) {
        super(message);
    }

    public JavaException(Throwable cause) {
        super(cause);
    }

    public JavaException(String message, Throwable cause) {
        super(message, cause);
    }

}
