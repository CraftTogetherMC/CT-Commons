package de.crafttogether.common.event.events;

import de.crafttogether.common.event.Event;
import de.crafttogether.common.event.Player;

public class PlayerQuitEvent implements Event {
    private final Player player;

    public PlayerQuitEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}