package me.heldplayer.irc.api.event;

public interface IEventBus {

    void registerEventHandler(Object obj);

    void unregisterEventHandler(Object obj);

    boolean postEvent(Event event);

    void cleanup();

}
