package me.heldplayer.irc.api.event;

public class CancellableEvent extends Event {

    private boolean isCancelled;

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

}
