package de.crafttogether.ctcommons;

import com.google.common.collect.ImmutableMap;
import de.crafttogether.CTCommons;
import de.crafttogether.common.commands.CloudSimpleHandler;
import de.crafttogether.common.configuration.file.FileConfiguration;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.messaging.MessagingClient;
import de.crafttogether.common.messaging.MessagingServer;
import de.crafttogether.common.messaging.MessagingService;
import de.crafttogether.common.mysql.LogFilter;
import de.crafttogether.common.platform.bukkit.CloudBukkitHandler;
import de.crafttogether.common.platform.bungeecord.CloudBungeeHandler;
import de.crafttogether.common.platform.velocity.CloudVelocityHandler;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.PluginInformation;
import de.crafttogether.common.util.AdventureUtil;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.ctcommons.commands.MessagingCommands;
import de.crafttogether.ctcommons.commands.ReloadCommand;
import de.crafttogether.ctcommons.commands.UpdateCommand;
import de.crafttogether.ctcommons.listener.PacketReceivedListener;
import de.crafttogether.ctcommons.listener.PlayerJoinListener;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.string.PropertyReplacingStringProcessor;

import java.io.InputStream;

public class CTCommonsCore {
    public static CTCommonsCore instance;
    private static PlatformAbstractionLayer platformLayer;
    private static LocalizationManager localizationManager;
    private static MessagingService messagingService;
    private static FileConfiguration config;
    private static CloudSimpleHandler cloud;

    protected void onEnable(PlatformAbstractionLayer _platformLayer) {
        instance = this;
        platformLayer = _platformLayer;

        // Filter Hikari logs
        LogFilter.registerFilter();

        // Create default config
        InputStream defaultConfig;
        if (platformLayer.isBungeeCord() || platformLayer.isVelocity())
            defaultConfig = platformLayer.getPluginInformation().getResourceFromJar("proxyconfig.yml");
        else
            defaultConfig = platformLayer.getPluginInformation().getResourceFromJar("config.yml");

        PluginUtil.saveDefaultConfig(platformLayer, defaultConfig);
        config = PluginUtil.getConfig(platformLayer);

        // Initialize Cloud
        cloud = switch (platformLayer.getPlatformType()) {
            case BUKKIT -> new CloudBukkitHandler();
            case BUNGEECORD -> new CloudBungeeHandler();
            case VELOCITY -> new CloudVelocityHandler();
        };
        cloud.enable();

        // Register properties
        cloud.getParser().stringProcessor(
            new PropertyReplacingStringProcessor(
                s -> ImmutableMap.of(
                    "plugin", platformLayer.getPluginInformation().getName().toLowerCase()
                ).get(s)
            )
        );

        // Register commands
        cloud.annotations(new ReloadCommand());
        cloud.annotations(new UpdateCommand());
        cloud.annotations(new MessagingCommands());

        // Register listener
        CTCommons.getEventManager().registerListener(platformLayer, new PlayerJoinListener());
        CTCommons.getEventManager().registerListener(platformLayer, new PacketReceivedListener());

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(platformLayer, Localization.class, "en_EN", "locales");
        localizationManager.loadLocalization(config.getString("Settings.Language"));
        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());

        // Start MessagingService
        if (getConfig().getBoolean("Messaging.Enabled")) {
            messagingService = new MessagingService();
            messagingService.enable();
        }

        // Check for updates
        if (!config.getBoolean("Updates.Notify.DisableNotifications") && config.getBoolean("Updates.Notify.Console"))
            UpdateCommand.getUpdateFeedback((err, feedback) -> AdventureUtil.getConsole().sendMessage(
                    Component.text("[" + platformLayer.getPluginInformation().getName() +  "]:")
                            .appendNewline()
                            .append(err == null ? feedback : Component.text(err.getMessage()))
            ), 0L);

        PluginInformation pluginInformation = platformLayer.getPluginInformation();
        platformLayer.getPluginLogger().info(pluginInformation.getName() + " v" + pluginInformation.getVersion() + " (build: " + pluginInformation.getBuild() + ") enabled.");
    }

    protected void onDisable(PlatformAbstractionLayer platform) {
        if (MessagingService.isEnabled())
            messagingService.disable();

        PluginInformation pluginInformation = platform.getPluginInformation();
        platform.getPluginLogger().info(pluginInformation.getName() + " v" + platform.getPluginInformation().getVersion() + " disabled.");
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public void reloadConfig() {
        config = PluginUtil.getConfig(platformLayer);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public PlatformAbstractionLayer getPlatform() {
        return platformLayer;
    }
}
