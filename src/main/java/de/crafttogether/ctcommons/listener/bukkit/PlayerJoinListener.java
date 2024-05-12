package de.crafttogether.ctcommons.listener.bukkit;

import de.crafttogether.ctcommons.CTCommons;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("ctcommons.notify.updates"))
            return;

        CTCommons.getInstance().onPlayerJoin(event.getPlayer().getUniqueId());
    }
}
