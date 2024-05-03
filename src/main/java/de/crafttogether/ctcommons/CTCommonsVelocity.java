package de.crafttogether.ctcommons;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.VelocityPlatformLayer;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "${project.artifactId}",
        name = "${project.name}",
        version = "${project.version}"
)
public class CTCommonsVelocity {
    public static CTCommonsVelocity plugin;
    public static PlatformAbstractionLayer platform;
    public static BungeeAudiences adventure;

    private final LocalizationManager localizationManager;

    @Inject
    private Logger logger;
    private final ProxyServer proxy;

    @Inject
    public CTCommonsVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;

        plugin = this;
        adventure = this.adventure();
        platform = new VelocityPlatformLayer(plugin, proxy, logger, dataDirectory);

        // bStats
        // Injected automatically?!

        // Startup
        CTCommons.onEnable(platform);
        localizationManager = CTCommons.localizationManager;
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public BungeeAudiences adventure() {
        return adventure;
    }
}
