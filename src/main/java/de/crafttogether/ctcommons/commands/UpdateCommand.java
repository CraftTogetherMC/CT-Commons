package de.crafttogether.ctcommons.commands;

import de.crafttogether.common.commands.CommandSender;
import de.crafttogether.ctcommons.CTCommons;
import de.crafttogether.ctcommons.Localization;
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

        CTCommons.getUpdateFeedback((err, feedback) -> {
            if (err != null)
                feedback = Component.text(err.getMessage());

            sender.sendMessage(feedback);
        }, 0L);
    }
}
