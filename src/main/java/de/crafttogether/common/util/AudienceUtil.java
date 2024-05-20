package de.crafttogether.common.util;

import de.crafttogether.CTCommons;
import de.crafttogether.ctcommons.CTCommonsBukkit;
import de.crafttogether.ctcommons.CTCommonsBungee;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;

import java.util.UUID;

import static de.crafttogether.CTCommons.plugin;

public class AudienceUtil {
    public static class Bukkit {
        public static BukkitAudiences audiences = CTCommonsBukkit.audiences;
    }
    public static class BungeeCord {
        public static BungeeAudiences audiences = CTCommonsBungee.audiences;
    }
    
    public static Audience getConsole() {
        return switch (plugin.getPlatform().getPlatformType()) {
            case BUKKIT -> Bukkit.audiences.console();
            case BUNGEECORD -> BungeeCord.audiences.console();
            case VELOCITY -> CTCommons.Velocity.proxy.getConsoleCommandSource();
        };
    }

    public static Audience getPlayer(UUID uuid) {
        return switch (plugin.getPlatform().getPlatformType()) {
            case BUKKIT -> Bukkit.audiences.player(uuid);
            case BUNGEECORD -> BungeeCord.audiences.player(uuid);
            case VELOCITY -> CTCommons.Velocity.proxy.getAllPlayers().stream().filter(c -> c.getUniqueId().equals(uuid)).findAny().orElse(null);
        };
    }

    public static Audience getPlayers() {
        return switch (plugin.getPlatform().getPlatformType()) {
            case BUKKIT -> Bukkit.audiences.players();
            case BUNGEECORD -> BungeeCord.audiences.players();
            case VELOCITY -> CTCommons.Velocity.proxy;
        };
    }

    public static Audience getPlayers(String serverName) {
        return switch (plugin.getPlatform().getPlatformType()) {
            case BUKKIT -> Bukkit.audiences.players();
            case BUNGEECORD -> BungeeCord.audiences.server(serverName);
            case VELOCITY -> CTCommons.Velocity.proxy.getAllServers().stream().filter(c -> c.getServerInfo().getName().equals(serverName)).findAny().orElse(null);
        };
    }
}
