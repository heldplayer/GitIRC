package me.heldplayer.irc.api.plugin;

public class PluginException extends RuntimeException {

    private static final long serialVersionUID = -2736882986422475890L;

    public PluginException() {
        super();
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(Throwable cause) {
        super(cause);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

}
