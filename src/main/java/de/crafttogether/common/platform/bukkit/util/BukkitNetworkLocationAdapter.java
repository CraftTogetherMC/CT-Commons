package de.crafttogether.common.platform.bukkit.util;

import de.crafttogether.common.NetworkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public final class BukkitNetworkLocationAdapter {

    private BukkitNetworkLocationAdapter() {
    }

    public static Location toBukkitLocation(NetworkLocation location) {
        if (location == null || Bukkit.getWorld(location.getWorld()) == null) {
            return null;
        }

        return new Location(
                Bukkit.getWorld(location.getWorld()),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    public static NetworkLocation fromBukkitLocation(Location location, String serverName) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        return new NetworkLocation(
                serverName,
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }
}