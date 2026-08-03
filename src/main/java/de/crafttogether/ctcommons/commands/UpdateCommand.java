package de.crafttogether.ctcommons.commands;

import de.crafttogether.common.commands.CommandSender;
import de.crafttogether.ctcommons.Localization;
import de.crafttogether.ctcommons.Update;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;

public class UpdateCommand {
    @Command("${plugin}")
    @CommandDescription("Check for plugin updates")
    public void ctcommons_update(
            final CommandSender sender
    ) {
        sender.sendMessage(Localization.UPDATE_CHECK.deserialize());
        Update.check((err, feedback) -> sender.sendMessage(err == null ? feedback : Component.text(err.getMessage())), 0L);
    }
}
