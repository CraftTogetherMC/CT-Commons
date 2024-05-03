package de.crafttogether.ctcommons;

import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.mysql.LogFilter;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.PluginInformation;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CTCommons {
    public static LocalizationManager localizationManager;

    private static PlatformAbstractionLayer platform;
    private static Configuration config;

    protected static void onEnable(PlatformAbstractionLayer _platform) {
        platform = _platform;

        // Filter Hikari logs
        LogFilter.registerFilter();

        // Create default config

        //TODO: Config
        makeConfig("config.yml");

        //TODO: Commands
        //registerCommand("ctcommons", this);

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(platform, Localization.class, "en_EN", "locales");
        localizationManager.loadLocalization(getConfig().getString("Settings.Language"));
        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());

        //TODO: Events
        // Register events
        //getServer().getPluginManager().registerEvents(this, this);

        // Check for updates
        if (!getConfig().getBoolean("Settings.Updates.Notify.DisableNotifications")
                && getConfig().getBoolean("Settings.Updates.Notify.Console"))
        {
            new UpdateChecker(this).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
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
            }, getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        Configuration pluginDescriptionFile = new net.md_5.bungee.config.Configuration()
        (platform.getPluginInformation().getDataFolder() + File.separator + "config.yml").getString("build");
        PluginInformation pluginInformation = platform.getPluginInformation();
        platform.getPluginLogger().info(pluginInformation.getName() + " v" + pluginInformation.getVersion() + " (" + build + ") enabled.");
    }

    protected static void onDisable(PlatformAbstractionLayer platform) {
        PluginInformation pluginInformation = platform.getPluginInformation();
        platform.getPluginLogger().info(pluginInformation.getName() + " v" + pluginInformation.getVersion() + " disabled.");
    }

    protected static void makeConfig(String fileName) {
        File dataFolder = platform.getPluginInformation().getDataFolder();
        // Create plugin config folder if it doesn't exist
        if (!dataFolder.exists()) {
            platform.getPluginLogger().info("Created config folder: " + dataFolder.mkdir());
        }

        File configFile = new File(dataFolder, fileName);

        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            try {
                FileOutputStream outputStream = new FileOutputStream(configFile);
                InputStream templateFile = platform.getPluginInformation().getResourceFromJar(fileName);
                templateFile.transferTo(outputStream);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static Configuration getConfig() {
        if (config == null) {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        }
        return config;
    }
}
