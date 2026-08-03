package de.crafttogether.ctcommons;

import de.crafttogether.common.plugin.BungeePlatformLayer;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.platform.bungeecord.listener.PostLoginListener;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

public final class CTCommonsBungee extends Plugin {
    private PlatformAbstractionLayer platformLayer;

    public static CTCommonsBungee plugin;
    public static BungeeAudiences audiences;

    private final CTCommonsCore CTCommonsInstance = new CTCommonsCore();

    @Override
    public void onEnable() {
        plugin = this;
        audiences = BungeeAudiences.create(plugin);
        platformLayer = new BungeePlatformLayer(plugin);

        // bStats
        new Metrics(plugin, 17413);

        // Register Listener
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, new PostLoginListener());

        // Startup
        CTCommonsInstance.onEnable(platformLayer);
    }

    @Override
    public void onDisable() {
        CTCommonsInstance.onDisable(platformLayer);
    }
}
