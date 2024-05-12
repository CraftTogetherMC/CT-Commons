package de.crafttogether.ctcommons;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.VelocityPlatformLayer;
import de.crafttogether.ctcommons.listener.velocity.PlayerChooseInitialServerListener;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "ctcommons",
        name = "CTCommonsVelocity",
        version = "1.0-BETA3",
        url = "https://github.com/CraftTogetherMC",
        description = "Library to centralize main functions for multiple plugins which will allow better maintaining capabilities and will lower general plugin sizes.",
        authors = {"J0schlZ"}
)

public class CTCommonsVelocity {
    public static CTCommonsVelocity plugin;
    public static ProxyServer proxy;
    public static PlatformAbstractionLayer platform;
    private final CTCommons CTCommons = new CTCommons();

    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public CTCommonsVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        CTCommonsVelocity.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        plugin = this;
        platform = new VelocityPlatformLayer(plugin, proxy, logger, dataDirectory);

        // Register Listener
        proxy.getEventManager().register(this, new PlayerChooseInitialServerListener());

        // Startup
        CTCommons.onEnable(platform);
    }
}
