package de.crafttogether.common.event;

/**
 * Dummy class which all callable events must extend.
 */
public interface Event {
    /**
     * Method called after this event has been dispatched to all handlers.
     */
    default void postCall() {}
}
