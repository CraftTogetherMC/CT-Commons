package de.crafttogether;

import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.ctcommons.Localization;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class CTCommons extends JavaPlugin implements Listener {
    public static CTCommons plugin;
    public static BukkitAudiences adventure;

    private LocalizationManager localizationManager;

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BukkitAudiences.create(this);

        // Create default config
        saveDefaultConfig();

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(this, Localization.class, getConfig().getString("Settings.Language"), "en_EN", "locales");
        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Check for updates
        if (!getConfig().getBoolean("Settings.Updates.Notify.DisableNotifications")
                && getConfig().getBoolean("Settings.Updates.Notify.Console"))
        {
            new UpdateChecker(this).checkUpdatesAsync((String version, String build, String fileName, Integer fileSize, String url, String currentVersion, String currentBuild, BuildType buildType) -> {
                if (buildType.equals(BuildType.UP2DATE))
                    return;

                switch (buildType) {
                    case RELEASE -> plugin.getLogger().warning("A new full version of this plugin was released!");
                    case SNAPSHOT -> plugin.getLogger().warning("A new snapshot version of this plugin is available!");
                }

                plugin.getLogger().warning("You can download it here: " + url);
                plugin.getLogger().warning("Version: " + version + " #" + build);
                plugin.getLogger().warning("FileName: " + fileName + " FileSize: " + UpdateChecker.humanReadableFileSize(fileSize));
                plugin.getLogger().warning("You are on version: " + currentVersion + " #" + currentBuild);

            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        getLogger().info(getName() + " v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info(getName() + " v" + getDescription().getVersion() + " disabled.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("ctcommons.notify.updates"))
            return;

        Configuration config = plugin.getConfig();

        if (config.getBoolean("Updates.Notify.DisableNotifications")
                || !config.getBoolean("Updates.Notify.InGame"))
            return;

        new UpdateChecker(plugin).checkUpdatesAsync((String version, String build, String fileName, Integer fileSize, String url, String currentVersion, String currentBuild, BuildType buildType) -> {
            List<Placeholder> resolvers = new ArrayList<>();
            Component message;

            resolvers.add(Placeholder.set("version", version));
            resolvers.add(Placeholder.set("build", build));
            resolvers.add(Placeholder.set("fileName", fileName));
            resolvers.add(Placeholder.set("fileSize", UpdateChecker.humanReadableFileSize(fileSize)));
            resolvers.add(Placeholder.set("url", url));
            resolvers.add(Placeholder.set("currentVersion", currentVersion));
            resolvers.add(Placeholder.set("currentBuild", currentBuild));

            if (buildType.equals(BuildType.RELEASE))
                message = Localization.UPDATE_RELEASE.deserialize(resolvers);
            else
                message = Localization.UPDATE_DEVBUILD.deserialize(resolvers);

            PluginUtil.adventure().player(event.getPlayer()).sendMessage(message);
        }, plugin.getConfig().getBoolean("Updates.CheckForDevBuilds"), 40L);
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public BukkitAudiences adventure() {
        return adventure;
    }
}
