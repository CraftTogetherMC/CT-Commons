package de.crafttogether.common.platform.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import de.crafttogether.CTCommons;
import de.crafttogether.common.event.events.PlayerJoinEvent;
import de.crafttogether.common.platform.velocity.VelocityPlayer;

public class PlayerChooseInitialServerListener {
    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        VelocityPlayer player = new VelocityPlayer(event.getPlayer());
        CTCommons.getEventManager().callEvent(new PlayerJoinEvent(player));
    }
}
