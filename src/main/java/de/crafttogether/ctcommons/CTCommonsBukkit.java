package de.crafttogether.ctcommons;

import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.plugin.BukkitPlatformLayer;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class CTCommonsBukkit extends JavaPlugin {
    public static CTCommonsBukkit plugin;
    public static PlatformAbstractionLayer platform;
    public static BukkitAudiences adventure;

    private LocalizationManager localizationManager;

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BukkitAudiences.create(plugin);
        platform = new BukkitPlatformLayer(plugin);

        // bStats
        new Metrics(plugin, 17413);

        // Startup
        CTCommons.onEnable(platform);
        localizationManager = CTCommons.localizationManager;
    }

    @Override
    public void onDisable() {
        CTCommons.onDisable(platform);
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public BukkitAudiences adventure() {
        return adventure;
    }
}
