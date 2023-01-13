package de.crafttogether.common;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Serializable;

@SuppressWarnings("unused")
public class NetworkLocation implements Serializable {
    private String server;
    private String world;
    private double x;
    private double y;
    private double z;

    public NetworkLocation(String server, String world, double x, double y, double z) {
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Gets the corresponding org.bukkit.Location of this location
     * @return The corresponding Location
     */
    public Location getBukkitLocation() {
        if (Bukkit.getWorld(this.world) == null)
            return null;

        return new Location(Bukkit.getWorld(this.world), this.getX(), this.getY(), this.getZ());
    }

    /**
     * Gets a new NetworkLocation based on the given org.bukkit.Location
     * @param location The location
     * @param serverName Name of the server of the location
     * @return
     */
    public static NetworkLocation fromBukkitLocation(Location location, String serverName) {
        if (location.getWorld() == null)
            return null;

        return new NetworkLocation(serverName, location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    public String getServer() {
        return server;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public String toString() {
        return "NetworkLocation{server=" + server + ", world=" + world + ", x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
