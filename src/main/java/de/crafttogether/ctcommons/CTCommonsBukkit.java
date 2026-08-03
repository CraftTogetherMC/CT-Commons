package de.crafttogether.ctcommons;

import de.crafttogether.common.platform.bukkit.events.PlayerJoinListener;
import de.crafttogether.common.plugin.BukkitPlatformLayer;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class CTCommonsBukkit extends JavaPlugin implements CommandExecutor, TabExecutor {
    private PlatformAbstractionLayer platformLayer;

    public static CTCommonsBukkit plugin;
    public static BukkitAudiences audiences;

    private final CTCommonsCore CTCommonsInstance = new CTCommonsCore();

    @Override
    public void onEnable() {
        plugin = this;
        audiences = BukkitAudiences.create(plugin);
        platformLayer = new BukkitPlatformLayer(plugin);

        // bStats
        new Metrics(plugin, 17413);

        // Event Listener
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        // Startup
        CTCommonsInstance.onEnable(platformLayer);
    }

    @Override
    public void onDisable() {
        CTCommonsInstance.onDisable(platformLayer);
    }
}
