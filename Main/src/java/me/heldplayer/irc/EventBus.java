
package me.heldplayer.irc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import me.heldplayer.irc.api.event.CancellableEvent;
import me.heldplayer.irc.api.event.Event;
import me.heldplayer.irc.api.event.EventHandler;
import me.heldplayer.irc.api.event.IEventBus;

class EventBus implements IEventBus {

    private HashSet<Object> eventHandlers = new HashSet<Object>();
    private HashMap<Class<? extends Event>, Events> methods = new HashMap<Class<? extends Event>, Events>();

    private static Object lock = new Object();

    @SuppressWarnings("unchecked")
    @Override
    public void registerEventHandler(Object obj) {
        synchronized (EventBus.lock) {
            if (!this.eventHandlers.contains(obj)) {
                this.eventHandlers.add(obj);

                Class<?> clazz = obj.getClass();
                Method[] methods = clazz.getDeclaredMethods();

                for (Method method : methods) {
                    if (method.isAnnotationPresent(EventHandler.class) && method.getParameterTypes().length == 1) {
                        Class<?> paramType = method.getParameterTypes()[0];
                        if (Event.class.isAssignableFrom(paramType)) {
                            if (this.methods.containsKey(paramType)) {
                                Events events = this.methods.get(paramType);
                                events.addMethod(obj, method);
                            }
                            else {
                                Events events = new Events();
                                events.addMethod(obj, method);
                                this.methods.put((Class<? extends Event>) paramType, events);
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unregisterEventHandler(Object obj) {
        synchronized (EventBus.lock) {
            if (this.eventHandlers.contains(obj)) {
                this.eventHandlers.remove(obj);

                Set<Entry<Class<? extends Event>, Events>> entries = this.methods.entrySet();
                Entry<Class<? extends Event>, Events>[] entryArray = entries.toArray(new Entry[entries.size()]);

                for (int i = 0; i < entryArray.length; i++) {
                    Entry<Class<? extends Event>, Events> entry = entryArray[i];
                    Events events = entry.getValue();

                    events.removeClass(obj.getClass());

                    if (events.classes.length == 0) {
                        this.methods.remove(entry.getKey());
                    }
                }
            }
        }
    }

    @Override
    public boolean postEvent(Event event) {
        synchronized (EventBus.lock) {
            Set<Entry<Class<? extends Event>, Events>> entries = this.methods.entrySet();

            for (Entry<Class<? extends Event>, Events> entry : entries) {
                Class<? extends Event> clazz = entry.getKey();

                if (clazz.isAssignableFrom(event.getClass())) {
                    Events events = entry.getValue();
                    for (MethodClassLink method : events.classes) {
                        try {
                            method.method.invoke(method.obj, event);
                        }
                        catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (event instanceof CancellableEvent) {
                return !((CancellableEvent) event).isCancelled();
            }
            return true;
        }
    }

    @Override
    public void cleanup() {
        this.methods.clear();
        this.methods = null;
        this.eventHandlers.clear();
        this.eventHandlers = null;
    }

    private static class Events {

        public MethodClassLink[] classes;

        public Events() {
            this.classes = new MethodClassLink[0];
        }

        public void addMethod(Object obj, Method method) {
            MethodClassLink[] newClasses = new MethodClassLink[this.classes.length + 1];
            System.arraycopy(this.classes, 0, newClasses, 0, this.classes.length);
            newClasses[this.classes.length] = new MethodClassLink(obj, method);
            this.classes = newClasses;
        }

        public void removeClass(Class<?> clazz) {
            MethodClassLink[] temp = new MethodClassLink[this.classes.length];
            int i = 0;
            for (MethodClassLink link : this.classes) {
                Class<?> theClazz = link.obj.getClass();
                if (theClazz != clazz) {
                    temp[i] = link;
                    i++;
                }
            }
            this.classes = new MethodClassLink[i];
            System.arraycopy(temp, 0, this.classes, 0, i);
        }

    }

    private static class MethodClassLink {

        public final Object obj;
        public final Method method;

        public MethodClassLink(Object obj, Method method) {
            this.obj = obj;
            this.method = method;
        }

    }

}
