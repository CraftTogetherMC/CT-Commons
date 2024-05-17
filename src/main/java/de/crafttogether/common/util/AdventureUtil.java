package de.crafttogether.common.util;

import de.crafttogether.CTCommons;
import net.kyori.adventure.audience.Audience;

import java.util.UUID;

import static de.crafttogether.CTCommons.plugin;

public class AdventureUtil {
    public static Audience getConsole() {
        return switch (plugin.getPlatform().getPlatformType()) {
            case BUKKIT -> CTCommons.Bukkit.audiences.console();
            case BUNGEECORD -> CTCommons.BungeeCord.audiences.console();
            case VELOCITY -> CTCommons.Velocity.proxy.getConsoleCommandSource();
        };
    }

    public static Audience getPlayer(UUID uuid) {
        return switch (plugin.getPlatform().getPlatformType()) {
            case BUKKIT -> CTCommons.Bukkit.audiences.player(uuid);
            case BUNGEECORD -> CTCommons.BungeeCord.audiences.player(uuid);
            case VELOCITY -> CTCommons.Velocity.proxy.getAllPlayers().stream().filter(c -> c.getUniqueId().equals(uuid)).findAny().orElse(null);
        };
    }

    public static Audience getPlayers() {
        return switch (plugin.getPlatform().getPlatformType()) {
            case BUKKIT -> CTCommons.Bukkit.audiences.players();
            case BUNGEECORD -> CTCommons.BungeeCord.audiences.players();
            case VELOCITY -> CTCommons.Velocity.proxy;
        };
    }

    public static Audience getPlayers(String serverName) {
        return switch (plugin.getPlatform().getPlatformType()) {
            case BUKKIT -> CTCommons.Bukkit.audiences.players();
            case BUNGEECORD -> CTCommons.BungeeCord.audiences.server(serverName);
            case VELOCITY -> CTCommons.Velocity.proxy.getAllServers().stream().filter(c -> c.getServerInfo().getName().equals(serverName)).findAny().orElse(null);
        };
    }
}
