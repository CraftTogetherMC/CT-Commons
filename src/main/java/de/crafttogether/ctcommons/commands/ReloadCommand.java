package de.crafttogether.ctcommons.commands;

import de.crafttogether.CTCommons;
import de.crafttogether.common.commands.CommandSender;
import de.crafttogether.ctcommons.Localization;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;

public class ReloadCommand {
    @Command("${plugin} reload")
    @CommandDescription("This command reloads the configuration of the plugin")
    public void ctcommons_reload(
            final CommandSender sender
    ) {
        CTCommons.getLogger().info("Reloading config.yml...");
        CTCommons.plugin.reloadConfig();

        CTCommons.getLogger().info("Reloading localization...");
        CTCommons.plugin.getLocalizationManager().loadLocalization(CTCommons.plugin.getConfig().getString("Settings.Language"));

        CTCommons.getLogger().info("Reload completed...");

        sender.sendMessage(Localization.CONFIG_RELOADED.deserialize());
    }
}
