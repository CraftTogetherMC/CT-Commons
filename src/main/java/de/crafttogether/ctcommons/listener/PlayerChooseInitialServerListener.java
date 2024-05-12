package de.crafttogether.ctcommons.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import de.crafttogether.common.Logging;
import de.crafttogether.ctcommons.CTCommons;

public class PlayerChooseInitialServerListener {
    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Logging.getLogger().info("#onPlayerChooseInitialServer");
        if (!event.getPlayer().hasPermission("ctcommons.notify.updates")) {
            Logging.getLogger().info("permission-check failed");
            return;
        }


        Logging.getLogger().info("CALL: onPlayerJoin()");
        CTCommons.getInstance().onPlayerJoin(event.getPlayer().getUniqueId());
    }
}
