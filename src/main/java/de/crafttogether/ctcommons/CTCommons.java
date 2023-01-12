package de.crafttogether.ctcommons;

import de.crafttogether.ctcommons.localization.LocalizationManager;
import de.crafttogether.ctcommons.update.BuildType;
import de.crafttogether.ctcommons.update.UpdateChecker;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CTCommons extends JavaPlugin {
    public static CTCommons plugin;

    private LocalizationManager localizationManager;
    private MiniMessage miniMessageParser;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BukkitAudiences.create(this);

        // Create default config
        saveDefaultConfig();

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(this, "de_DE", "en_EN", "locales");

        // Check for updates
        if (!getConfig().getBoolean("Updates.Notify.DisableNotifications"))
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

            }, plugin.getConfig().getBoolean("Updates.CheckForDevBuilds"));
        }

        getLogger().info(getName() + " v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info(getName() + " v" + getDescription().getVersion() + " disabled.");
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public MiniMessage getMiniMessageParser() {
        return Objects.requireNonNullElseGet(miniMessageParser, MiniMessage::miniMessage);
    }

    public BukkitAudiences adventure() {
        return adventure;
    }
}
