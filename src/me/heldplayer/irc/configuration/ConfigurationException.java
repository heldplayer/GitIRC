
package me.heldplayer.irc.configuration;

public class ConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 5241842561838003345L;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
