package de.crafttogether.ctcommons.listener;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.EventListener;
import de.crafttogether.common.event.Listener;
import de.crafttogether.common.event.events.PlayerJoinEvent;
import de.crafttogether.ctcommons.commands.UpdateCommand;
import net.kyori.adventure.text.Component;

public class PlayerJoinListener implements Listener {

    @EventListener
    public void on(PlayerJoinEvent event) {
        CTCommons.getLogger().info("#Common #PlayerLoginEvent " + event.getPlayer().getName());

        if (!event.getPlayer().hasPermission("ctcommons.notify.updates"))
            return;

        UpdateCommand.getUpdateFeedback((err, feedback) -> event.getPlayer().sendMessage(err == null ? feedback : Component.text(err.getMessage())), 0L);
    }
}
