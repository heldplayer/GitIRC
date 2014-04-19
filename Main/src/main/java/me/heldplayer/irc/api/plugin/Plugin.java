
package me.heldplayer.irc.api.plugin;

import java.util.logging.Logger;

public abstract class Plugin {

    private boolean enabled;
    PluginInfo info;
    PluginClassLoader loader;
    Logger logger;

    public final boolean isEnabled() {
        return this.enabled;
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            if (enabled) {
                this.onEnable();
                this.enabled = true;
            }
            else {
                this.enabled = false;
                this.onDisable();
            }
        }
    }

    public final PluginInfo getInfo() {
        return this.info;
    }

    public final Logger getLogger() {
        return this.logger;
    }

    public abstract void onEnable();

    public abstract void onDisable();

}
