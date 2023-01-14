package de.crafttogether;

import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.update.Build;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.ctcommons.Localization;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.update.UpdateChecker;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CTCommons extends JavaPlugin implements Listener, CommandExecutor {
    public static CTCommons plugin;
    public static BukkitAudiences adventure;

    private LocalizationManager localizationManager;

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BukkitAudiences.create(this);

        // Create default config
        saveDefaultConfig();
        Objects.requireNonNull(getCommand("ctcommons")).setExecutor(this);

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(this, Localization.class, getConfig().getString("Settings.Language"), "en_EN", "locales");
        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Check for updates
        if (!getConfig().getBoolean("Settings.Updates.Notify.DisableNotifications")
                && getConfig().getBoolean("Settings.Updates.Notify.Console"))
        {
            new UpdateChecker(this).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
                if (err != null) {
                    err.printStackTrace();
                    return;
                }

                // No updates found, we are using the latest version
                if (build == null)
                    return;

                switch (build.getType()) {
                    case RELEASE -> plugin.getLogger().warning("A new full version of this plugin was released!");
                    case SNAPSHOT -> plugin.getLogger().warning("A new snapshot version of this plugin is available!");
                }

                plugin.getLogger().warning("You can download it here: " + build.getUrl());
                plugin.getLogger().warning("Version: " + build.getVersion() + " #" + build.getNumber());
                plugin.getLogger().warning("FileName: " + build.getFileName() + " FileSize: " + build.getHumanReadableFileSize());
                plugin.getLogger().warning("You are on version: " + currentVersion + " #" + currentBuild);

            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        getLogger().info(getName() + " v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info(getName() + " v" + getDescription().getVersion() + " disabled.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        new UpdateChecker(plugin).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
            if (err != null)
                err.printStackTrace();

            List<Placeholder> resolvers = new ArrayList<>();
            Component message;

            if (build == null) {
                resolvers.add(Placeholder.set("currentVersion", currentVersion));
                resolvers.add(Placeholder.set("currentBuild", currentBuild));

                message = plugin.getLocalizationManager().miniMessage()
                        .deserialize("<prefix/><gold>" + plugin.getName() + " version: </gold><yellow>" + currentVersion + " #" + currentBuild + "</yellow><newLine/>");

                if (err == null)
                    message = message.append(Localization.UPDATE_LASTBUILD.deserialize(resolvers));
                else
                    message = message.append(Localization.UPDATE_ERROR.deserialize(
                            Placeholder.set("error", err.getMessage())));
            }
            else
                message = feedback(build, currentVersion, currentBuild);

            PluginUtil.adventure().sender(sender).sendMessage(message);
        }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("ctcommons.notify.updates"))
            return;

        Configuration config = plugin.getConfig();

        if (config.getBoolean("Updates.Notify.DisableNotifications")
                || !config.getBoolean("Updates.Notify.InGame"))
            return;

        new UpdateChecker(plugin).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
            if (err != null) {
                err.printStackTrace();
                return;
            }

            if (build == null)
                return;

            PluginUtil.adventure().player(event.getPlayer()).sendMessage(feedback(build, currentVersion, currentBuild));
        }, plugin.getConfig().getBoolean("Updates.CheckForDevBuilds"), 40L);
    }

    private Component feedback(Build build, String currentVersion, String currentBuild) {
        List<Placeholder> resolvers = new ArrayList<>();
        resolvers.add(Placeholder.set("version", build.getVersion()));
        resolvers.add(Placeholder.set("build", build.getNumber()));
        resolvers.add(Placeholder.set("fileName", build.getFileName()));
        resolvers.add(Placeholder.set("fileSize", build.getHumanReadableFileSize()));
        resolvers.add(Placeholder.set("url", build.getUrl()));
        resolvers.add(Placeholder.set("currentVersion", currentVersion));
        resolvers.add(Placeholder.set("currentBuild", currentBuild));

        return switch (build.getType()) {
            case RELEASE -> Localization.UPDATE_RELEASE.deserialize(resolvers);
            case SNAPSHOT -> Localization.UPDATE_DEVBUILD.deserialize(resolvers);
        };
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public BukkitAudiences adventure() {
        return adventure;
    }
}
