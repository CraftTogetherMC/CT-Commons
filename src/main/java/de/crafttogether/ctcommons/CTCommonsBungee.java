package de.crafttogether.ctcommons;

import de.crafttogether.common.plugin.BungeePlatformLayer;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.ctcommons.listener.PostLoginListener;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

public final class CTCommonsBungee extends Plugin {
    public static CTCommonsBungee plugin;
    public static PlatformAbstractionLayer platform;
    public static BungeeAudiences adventure;

    private final CTCommons CTCommons = new CTCommons();

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BungeeAudiences.create(plugin);
        platform = new BungeePlatformLayer(plugin);

        // bStats
        new Metrics(plugin, 17413);

        // Register Listener
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, new PostLoginListener());

        // Startup
        CTCommons.onEnable(platform);
    }

    @Override
    public void onDisable() {
        CTCommons.onDisable(platform);
    }
}
