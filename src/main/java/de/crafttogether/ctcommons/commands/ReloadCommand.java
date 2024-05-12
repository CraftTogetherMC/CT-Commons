package de.crafttogether.ctcommons.commands;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import de.crafttogether.common.Logging;
import de.crafttogether.common.commands.CommandSender;
import de.crafttogether.ctcommons.CTCommons;
import de.crafttogether.ctcommons.Localization;

public class ReloadCommand {

    public ReloadCommand() {
        Logging.getLogger().info("CMD: ReloadCommand loaded!");
    }
    @CommandMethod("${plugin} reload")
    @CommandDescription("This command reloads the configuration of the plugin")
    public void tcdestinations_reload(
            final CommandSender sender
    ) {
        Logging.getLogger().info("Reloading config.yml...");
        CTCommons.reloadConfig();

        Logging.getLogger().info("Reloading localization...");
        CTCommons.getLocalizationManager().loadLocalization(CTCommons.getConfig().getString("Settings.Language"));

        Logging.getLogger().info("Reload completed...");

        sender.sendMessage(Localization.CONFIG_RELOADED.deserialize());
    }
}
