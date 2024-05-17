package de.crafttogether.ctcommons.commands;

import de.crafttogether.CTCommons;
import de.crafttogether.common.Consumer;
import de.crafttogether.common.commands.CommandSender;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.ctcommons.Localization;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;

import java.util.ArrayList;
import java.util.List;

public class UpdateCommand {
    @Command("${plugin}")
    @CommandDescription("Check for plugin updates")
    public void ctcommons_update(
            final CommandSender sender
    ) {
        sender.sendMessage(Localization.UPDATE_CHECK.deserialize());
        getUpdateFeedback((err, feedback) -> sender.sendMessage(err == null ? feedback : Component.text(err.getMessage())), 0L);
    }

    public static void getUpdateFeedback(Consumer consumer, Long delay) {
        new UpdateChecker(CTCommons.getPlatform()).checkUpdatesAsync("CTCommons", (err, installedVersion, installedBuild, build) -> {
            if (err != null) {
                CTCommons.getLogger().warn("An error occurred while receiving update information.");
                CTCommons.getLogger().warn("Error: " + err.getMessage());
                consumer.operation(err, Component.text(err.getMessage()));
            }

            if (build == null)
                return;

            List<Placeholder> resolvers = new ArrayList<>();
            resolvers.add(Placeholder.set("installedVersion", installedVersion));
            resolvers.add(Placeholder.set("installedBuild", installedBuild));
            resolvers.add(Placeholder.set("currentVersion", build.getVersion()));
            resolvers.add(Placeholder.set("currentBuild", build.getNumber()));
            resolvers.add(Placeholder.set("fileName", build.getFileName()));
            resolvers.add(Placeholder.set("fileSize", build.getHumanReadableFileSize()));
            resolvers.add(Placeholder.set("url", build.getUrl()));

            if (build.getType().equals(BuildType.RELEASE))
                consumer.operation(err, Localization.UPDATE_RELEASE.deserialize(resolvers));
            else
                consumer.operation(err, Localization.UPDATE_DEVBUILD.deserialize(resolvers));

        }, CTCommons.plugin.getConfig().getBoolean("Updates.CheckForDevBuilds"), delay);
    }
}
