package de.crafttogether.common.event;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;
import de.crafttogether.CTCommons;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;

import java.lang.reflect.Method;
import java.util.Iterator;

public class EventManager {
    private final EventBus eventBus = new EventBus(); // TODO: Logger
    private final Multimap<PlatformAbstractionLayer, Listener> listenersByPlugin = ArrayListMultimap.create();

    /**
     * Dispatch an event to all subscribed listeners and return the event once
     * it has been handled by these listeners.
     *
     * @param <T> the type bounds, must be a class which extends event
     * @param event the event to call
     * @return the called event
     */
    public <T extends Event> T callEvent(T event) {
        Preconditions.checkNotNull(event, "event");

        long start = System.nanoTime();
        eventBus.post(event);
        event.postCall();

        long elapsed = System.nanoTime() - start;
        if (elapsed > 250000000) {
            CTCommons.getLogger().warn(
                    "Event " + event + " took " + elapsed / 1000000 + "ms to process!");
        }
        return event;
    }

    public void registerListener(PlatformAbstractionLayer platformLayer, Listener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            Preconditions.checkArgument(!method.isAnnotationPresent(Subscribe.class),
                    "Listener %s has registered using deprecated subscribe annotation! Please update to @EventHandler.",
                    listener);
        }
        eventBus.register(listener);
        listenersByPlugin.put(platformLayer, listener);
    }

    /**
     * Unregister a {@link Listener} so that the events do not reach it anymore.
     *
     * @param listener the listener to unregister
     */
    public void unregisterListener(Listener listener) {
        eventBus.unregister(listener);
        listenersByPlugin.values().remove(listener);
    }

    /**
     * Unregister all of a Plugin's listener.
     *
     * @param platformLayer target plugin
     */
    public void unregisterListeners(PlatformAbstractionLayer platformLayer) {
        for (Iterator<Listener> it =
             listenersByPlugin.get(platformLayer).iterator();
             it.hasNext();) {
            eventBus.unregister(it.next());
            it.remove();
        }
    }
}
