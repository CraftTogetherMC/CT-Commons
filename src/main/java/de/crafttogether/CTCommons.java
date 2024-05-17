package de.crafttogether;

import de.crafttogether.common.event.EventManager;
import de.crafttogether.common.plugin.Platform;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.PluginInformation;
import de.crafttogether.common.plugin.scheduling.RunnableFactory;
import de.crafttogether.common.plugin.server.PluginLogger;
import de.crafttogether.ctcommons.CTCommonsBukkit;
import de.crafttogether.ctcommons.CTCommonsBungee;
import de.crafttogether.ctcommons.CTCommonsCore;
import de.crafttogether.ctcommons.CTCommonsVelocity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;

public class CTCommons {
    public static CTCommonsCore plugin = CTCommonsCore.instance;
    private static final EventManager eventManager = new EventManager();

    public static boolean isBukkit() {
        return plugin.getPlatform().isBukkit();
    }
    public static boolean isBungeeCord() {
        return plugin.getPlatform().isBungeeCord();
    }
    public static boolean isVelocity() {
        return plugin.getPlatform().isVelocity();
    }

    public static class Bukkit {
        public static CTCommonsBukkit plugin = CTCommonsBukkit.plugin;
        public static BukkitAudiences audiences = CTCommonsBukkit.audiences;
    }
    public static class BungeeCord {
        public static CTCommonsBungee plugin = CTCommonsBungee.plugin;
        public static BungeeAudiences audiences = CTCommonsBungee.audiences;
    }
    public static class Velocity {
        public static CTCommonsVelocity plugin = CTCommonsVelocity.plugin;
        public static com.velocitypowered.api.proxy.ProxyServer proxy = CTCommonsVelocity.proxy;
        public static com.velocitypowered.api.plugin.PluginContainer pluginContainer = CTCommonsVelocity.pluginContainer;
    }

    public static PlatformAbstractionLayer getPlatform() {
        return plugin.getPlatform();
    }
    public static Platform getPlatformType() {
        return plugin.getPlatform().getPlatformType();
    }
    public static RunnableFactory getRunnableFactory() {
        return plugin.getPlatform().getRunnableFactory();
    }
    public static PluginInformation getPluginInformation() {
        return plugin.getPlatform().getPluginInformation();
    }
    public static PluginLogger getLogger() {
        return plugin.getPlatform().getPluginLogger();
    }
    public static EventManager getEventManager() {
        return eventManager;
    }
}
