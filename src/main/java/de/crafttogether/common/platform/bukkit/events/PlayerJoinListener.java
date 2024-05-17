package de.crafttogether.common.platform.bukkit.events;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.events.PlayerJoinEvent;
import de.crafttogether.common.platform.bukkit.BukkitPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerJoinListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        BukkitPlayer player = new BukkitPlayer(event.getPlayer());
        CTCommons.getEventManager().callEvent(new PlayerJoinEvent(player));
    }
}