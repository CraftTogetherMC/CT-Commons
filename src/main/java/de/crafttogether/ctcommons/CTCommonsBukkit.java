package de.crafttogether.ctcommons;

import de.crafttogether.common.plugin.BukkitPlatformLayer;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.ctcommons.listener.PlayerJoinListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class CTCommonsBukkit extends JavaPlugin implements CommandExecutor, TabExecutor {
    public static CTCommonsBukkit plugin;
    public static PlatformAbstractionLayer platform;
    public static BukkitAudiences adventure;

    private final CTCommons CTCommonsInstance = new CTCommons();

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BukkitAudiences.create(plugin);
        platform = new BukkitPlatformLayer(plugin);

        // bStats
        new Metrics(plugin, 17413);

        // Event Listener
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        // Startup
        CTCommonsInstance.onEnable(platform);
    }

    @Override
    public void onDisable() {
        CTCommonsInstance.onDisable(platform);
    }
}
