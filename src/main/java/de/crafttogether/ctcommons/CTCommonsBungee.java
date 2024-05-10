package de.crafttogether.ctcommons;

import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.plugin.BungeePlatformLayer;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

public final class CTCommonsBungee extends Plugin {
    public static CTCommonsBungee plugin;
    public static PlatformAbstractionLayer platform;
    public static BungeeAudiences adventure;

    private LocalizationManager localizationManager;

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BungeeAudiences.create(plugin);
        platform = new BungeePlatformLayer(plugin);

        // bStats
        new Metrics(plugin, 17413);

        // Startup
        CTCommons.onEnable(platform);
        localizationManager = CTCommons.getLocalizationManager();
    }

    @Override
    public void onDisable() {
        CTCommons.onDisable(platform);
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public BungeeAudiences adventure() {
        return adventure;
    }
}
