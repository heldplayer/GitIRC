
package me.heldplayer.util.json;

public class JSONException extends RuntimeException {

    private static final long serialVersionUID = -8324459442544786192L;

    public JSONException() {
        super();
    }

    public JSONException(String message) {
        super(message);
    }

    public JSONException(Throwable cause) {
        super(cause);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }

}
