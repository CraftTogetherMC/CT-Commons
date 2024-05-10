package de.crafttogether.ctcommons;

import de.crafttogether.common.configuration.file.FileConfiguration;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.mysql.LogFilter;
import de.crafttogether.common.plugin.Platform;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.PluginInformation;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.PluginUtil;

import java.io.InputStream;

public class CTCommons {
    private static LocalizationManager localizationManager;

    private static PlatformAbstractionLayer platform;
    private static FileConfiguration config;

    protected static void onEnable(PlatformAbstractionLayer _platform) {
        platform = _platform;

        // Filter Hikari logs
        LogFilter.registerFilter();

        // Create default config

        //TODO: Config
        InputStream defaultConfig;
        if (platform.getPlatform().equals(Platform.BUNGEECORD) || platform.getPlatform().equals(Platform.VELOCITY)) {
            defaultConfig = platform.getPluginInformation().getResourceFromJar("proxyconfig.yml");
        }
        else {
            defaultConfig = platform.getPluginInformation().getResourceFromJar("config.yml");
        }

        PluginUtil.saveDefaultConfig(platform, defaultConfig);
        config = PluginUtil.getConfig(platform);


        //TODO: Commands
        //registerCommand("ctcommons", this);

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(platform, Localization.class, "en_EN", "locales");
        localizationManager.loadLocalization(config.getString("Settings.Language"));
        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());

        //TODO: Events
        // Register events
        //getServer().getPluginManager().registerEvents(this, this);

        // Check for updates
        if (!config.getBoolean("Updates.Notify.DisableNotifications")
                && config.getBoolean("Updates.Notify.Console"))
        {
            new UpdateChecker(platform).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
                if (err != null) {
                    platform.getPluginLogger().warn(" An error occurred while receiving update information.");
                    platform.getPluginLogger().warn("Error: " + err.getMessage());
                    return;
                }

                // No updates found, we are using the latest version
                if (build == null)
                    return;

                // Go sync again to avoid mixing output with other plugins
                platform.getRunnableFactory().create(() -> {
                    if (build.getType().equals(BuildType.RELEASE))
                        platform.getPluginLogger().warn("A new full version of this plugin was released!");
                    else
                        platform.getPluginLogger().warn("A new development version of this plugin is available!");

                    platform.getPluginLogger().warn("You can download it here: " + build.getUrl());
                    platform.getPluginLogger().warn("Version: " + build.getVersion() + " (build: " + build.getNumber() + ")");
                    platform.getPluginLogger().warn("FileName: " + build.getFileName() + " FileSize: " + build.getHumanReadableFileSize());
                    platform.getPluginLogger().warn("You are on version: " + currentVersion + " (build: " + currentBuild + ")");
                }).runTask();
            }, config.getBoolean("Updates.CheckForDevBuilds"));
        }

        PluginInformation pluginInformation = platform.getPluginInformation();
        platform.getPluginLogger().info(pluginInformation.getName() + " v" + pluginInformation.getVersion() + " (" + pluginInformation.getBuild() + ") enabled.");
    }

    protected static void onDisable(PlatformAbstractionLayer platform) {
        PluginInformation pluginInformation = platform.getPluginInformation();
        platform.getPluginLogger().info(pluginInformation.getName() + " v" + platform.getPluginInformation().getVersion() + " disabled.");
    }

    public static LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    public static PlatformAbstractionLayer getPlatform() {
        return platform;
    }
}
