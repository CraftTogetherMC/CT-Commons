package de.crafttogether;

import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.mysql.LogFilter;
import de.crafttogether.common.update.Build;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.ctcommons.Localization;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CTCommons extends JavaPlugin implements Listener, TabExecutor {
    public static CTCommons plugin;
    public static BukkitAudiences adventure;

    private LocalizationManager localizationManager;

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BukkitAudiences.create(this);

        // Filter Hikari logs
        LogFilter.registerFilter();

        // Create default config
        saveDefaultConfig();
        registerCommand("ctcommons", this);

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(this, Localization.class, "en_EN", "locales");
        localizationManager.loadLocalization(getConfig().getString("Settings.Language"));
        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Check for updates
        if (!getConfig().getBoolean("Settings.Updates.Notify.DisableNotifications")
                && getConfig().getBoolean("Settings.Updates.Notify.Console"))
        {
            new UpdateChecker(this).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
                if (err != null) {
                    plugin.getLogger().warning(" An error occurred while receiving update information.");
                    plugin.getLogger().warning("Error: " + err.getMessage());
                    return;
                }

                // No updates found, we are using the latest version
                if (build == null)
                    return;

                // Go sync again to avoid mixing output with other plugins
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (build.getType().equals(BuildType.RELEASE))
                        plugin.getLogger().warning("A new full version of this plugin was released!");
                    else
                        plugin.getLogger().warning("A new development version of this plugin is available!");

                    plugin.getLogger().warning("You can download it here: " + build.getUrl());
                    plugin.getLogger().warning("Version: " + build.getVersion() + " (build: " + build.getNumber() + ")");
                    plugin.getLogger().warning("FileName: " + build.getFileName() + " FileSize: " + build.getHumanReadableFileSize());
                    plugin.getLogger().warning("You are on version: " + currentVersion + " (build: " + currentBuild + ")");
                });
            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        // bStats
        new Metrics(this, 17413);

        String build = PluginUtil.getPluginFile(this).getString("build");
        getLogger().info(getName() + " v" + getDescription().getVersion() + " (" + build + ") enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info(getName() + " v" + getDescription().getVersion() + " disabled.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.getLogger().info("Reloading config.yml...");
            plugin.reloadConfig();

            plugin.getLogger().info("Reloading localization...");
            plugin.getLocalizationManager().loadLocalization(plugin.getConfig().getString("Settings.Language"));

            plugin.getLogger().info("Reload completed...");
            PluginUtil.adventure().sender(sender).sendMessage(Localization.CONFIG_RELOADED.deserialize());
        }

        else if (args.length == 0) {
            new UpdateChecker(plugin).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
                if (err != null)
                    plugin.getLogger().warning("An error occurred while receiving update information.");
                    plugin.getLogger().warning("Error: " + err.getMessage());

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
                } else
                    message = feedback(build, currentVersion, currentBuild);

                PluginUtil.adventure().sender(sender).sendMessage(message);
            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1)
            suggestions.add("reload");

        return suggestions;
    }

    public void registerCommand(String cmd, TabExecutor executor) {
        Objects.requireNonNull(plugin.getCommand(cmd)).setExecutor(executor);
        Objects.requireNonNull(plugin.getCommand(cmd)).setTabCompleter(executor);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("ctcommons.notify.updates"))
            return;

        Configuration config = plugin.getConfig();

        plugin.getLogger().info("Settings.Updates.Notify.DisableNotifications: " + config.getBoolean("Updates.Notify.DisableNotifications"));
        plugin.getLogger().info("Settings.Updates.Notify.InGame: " + config.getBoolean("Updates.Notify.InGame"));

        if (config.getBoolean("Settings.Updates.Notify.DisableNotifications")
                || !config.getBoolean("Settings.Updates.Notify.InGame"))
            return;

        new UpdateChecker(plugin).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
            if (err != null) {
                plugin.getLogger().warning("An error occurred while receiving update information.");
                plugin.getLogger().warning("Error: " + err.getMessage());
                return;
            }

            if (build == null)
                return;

            PluginUtil.adventure().player(event.getPlayer()).sendMessage(feedback(build, currentVersion, currentBuild));
        }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"), 40L);
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

        if (build.getType().equals(BuildType.RELEASE))
            return Localization.UPDATE_RELEASE.deserialize(resolvers);
        else
            return Localization.UPDATE_DEVBUILD.deserialize(resolvers);
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public BukkitAudiences adventure() {
        return adventure;
    }
}
