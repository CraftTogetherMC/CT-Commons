package de.crafttogether.common.platform.bungeecord.listener;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.events.PlayerJoinEvent;
import de.crafttogether.common.platform.bungeecord.BungeePlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PostLoginListener implements Listener {
    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        BungeePlayer player = new BungeePlayer(event.getPlayer());
        CTCommons.getEventManager().callEvent(new PlayerJoinEvent(player));
    }
}
