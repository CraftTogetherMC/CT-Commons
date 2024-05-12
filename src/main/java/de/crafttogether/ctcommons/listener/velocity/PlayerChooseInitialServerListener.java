package de.crafttogether.ctcommons.listener.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import de.crafttogether.ctcommons.CTCommons;

public class PlayerChooseInitialServerListener {
    @Subscribe
    public void onPlayerChat(PlayerChooseInitialServerEvent event) {
        if (!event.getPlayer().hasPermission("upates.not"))
            return;

        CTCommons.getInstance().onPlayerJoin(event.getPlayer().getUniqueId());
    }
}
