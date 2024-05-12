package de.crafttogether.ctcommons;

import com.google.common.collect.ImmutableMap;
import de.crafttogether.common.Logging;
import de.crafttogether.common.commands.CloudSimpleHandler;
import de.crafttogether.common.commands.platform.bukkit.CloudBukkitHandler;
import de.crafttogether.common.commands.platform.bungeecord.CloudBungeeHandler;
import de.crafttogether.common.commands.platform.velocity.CloudVelocityHandler;
import de.crafttogether.common.configuration.file.FileConfiguration;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.mysql.LogFilter;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.PluginInformation;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.ctcommons.commands.ReloadCommand;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.string.PropertyReplacingStringProcessor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CTCommons {
    private static CTCommons instance;
    private static LocalizationManager localizationManager;
    private static PlatformAbstractionLayer platform;
    private static FileConfiguration config;

    private static CloudSimpleHandler cloud;

    public interface Consumer {
        void operation(Component feedback);
    }

    protected void onEnable(PlatformAbstractionLayer _platform) {
        instance = this;
        platform = _platform;

        // Filter Hikari logs
        LogFilter.registerFilter();

        // Create default config
        InputStream defaultConfig;
        if (platform.isBungeeCord() || platform.isVelocity())
            defaultConfig = platform.getPluginInformation().getResourceFromJar("proxyconfig.yml");
        else
            defaultConfig = platform.getPluginInformation().getResourceFromJar("config.yml");

        PluginUtil.saveDefaultConfig(platform, defaultConfig);
        config = PluginUtil.getConfig(platform);

        // Initialize Cloud
        cloud = switch (platform.getPlatformType()) {
            case BUKKIT -> new CloudBukkitHandler();
            case BUNGEECORD -> new CloudBungeeHandler();
            case VELOCITY -> new CloudVelocityHandler();
        };

        cloud.enable();
        cloud.getParser().stringProcessor(
            new PropertyReplacingStringProcessor(
                s -> ImmutableMap.of(
                    "plugin", platform.getPluginInformation().getName().toLowerCase()
                ).get(s)
            )
        );

        // Register commands
        cloud.annotations(new ReloadCommand());

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(platform, Localization.class, "en_EN", "locales");
        localizationManager.loadLocalization(config.getString("Settings.Language"));
        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());

        // Check for updates
        if (!config.getBoolean("Updates.Notify.DisableNotifications") && config.getBoolean("Updates.Notify.Console")) {
            new UpdateChecker(platform).checkUpdatesAsync("CTCommons", (err, installedVersion, installedBuild, build) -> {
                platform.getRunnableFactory().create(() -> {
                    if (err != null) {
                        Logging.getLogger().warn(" An error occurred while receiving update information.");
                        Logging.getLogger().warn("Error: " + err.getMessage());
                        return;
                    }

                    if (build != null) {
                        if (build.getType().equals(BuildType.RELEASE))
                            Logging.getLogger().warn("A new full version of this plugin was released!");
                        else
                            Logging.getLogger().warn("A new development version of this plugin is available!");

                        Logging.getLogger().warn("You can download it here: " + build.getUrl());
                        Logging.getLogger().warn("Version: " + build.getVersion() + " (build: " + build.getNumber() + ")");
                        Logging.getLogger().warn("FileName: " + build.getFileName() + " FileSize: " + build.getHumanReadableFileSize());
                        Logging.getLogger().warn("You are on version: " + installedVersion + " (build: " + installedBuild + ")");
                    }
                }).runTask();
            }, config.getBoolean("Updates.CheckForDevBuilds"));
        }

        PluginInformation pluginInformation = platform.getPluginInformation();
        platform.getPluginLogger().info(pluginInformation.getName() + " v" + pluginInformation.getVersion() + " (build: " + pluginInformation.getBuild() + ") enabled.");
    }

    protected void onDisable(PlatformAbstractionLayer platform) {
        PluginInformation pluginInformation = platform.getPluginInformation();
        platform.getPluginLogger().info(pluginInformation.getName() + " v" + platform.getPluginInformation().getVersion() + " disabled.");
    }

    public void onPlayerJoin(UUID uuid) {
        Logging.getLogger().info("#onPlayerJoin triggered");

        platform.getRunnableFactory().create(() -> {
            FileConfiguration config = CTCommons.getConfig();

            if (config.getBoolean("Updates.Notify.DisableNotifications")
                    || !config.getBoolean("Updates.Notify.InGame"))
                return;

            getUpdateFeedback((feedback) -> {
                switch (platform.getPlatformType()) {
                    case BUKKIT -> CTCommonsBukkit.adventure.player(uuid).sendMessage(feedback);
                    case BUNGEECORD -> CTCommonsBungee.adventure.player(uuid).sendMessage(feedback);
                    case VELOCITY -> CTCommonsVelocity.proxy.getPlayer(uuid).ifPresent((player -> player.sendMessage(feedback)));
                }
            });
        }).runTask();
    }

    public static void getUpdateFeedback(Consumer consumer) {
        new UpdateChecker(CTCommons.getPlatform()).checkUpdatesAsync("CTCommons", (err, installedVersion, installedBuild, build) -> {
            if (err != null) {
                Logging.getLogger().warn("An error occurred while receiving update information.");
                Logging.getLogger().warn("Error: " + err.getMessage());
                consumer.operation(Component.text(err.getMessage()));
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
                consumer.operation(Localization.UPDATE_RELEASE.deserialize(resolvers));
            else
                consumer.operation(Localization.UPDATE_DEVBUILD.deserialize(resolvers));

        }, config.getBoolean("Updates.CheckForDevBuilds"), 40L);
    }


    public static LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public static void reloadConfig() {
        config = PluginUtil.getConfig(platform);
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    public static PlatformAbstractionLayer getPlatform() {
        return platform;
    }

    public static CTCommons getInstance() {
        return instance;
    }
}
