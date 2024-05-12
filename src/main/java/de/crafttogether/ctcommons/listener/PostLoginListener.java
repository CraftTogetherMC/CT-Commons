package de.crafttogether.ctcommons.listener;

import de.crafttogether.ctcommons.CTCommons;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PostLoginListener implements Listener {
    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        if (!event.getPlayer().hasPermission("ctcommons.notify.updates"))
            return;

        CTCommons.getInstance().onPlayerJoin(event.getPlayer().getUniqueId());
    }
}
