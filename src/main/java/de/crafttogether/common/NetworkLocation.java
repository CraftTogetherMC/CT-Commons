package de.crafttogether.common;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Serializable;

/**
 * Representation of a Location in a BungeeCord network
 **/

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
     * @return Location
     */
    public Location getBukkitLocation() {
        if (Bukkit.getWorld(this.world) == null)
            return null;

        return new Location(Bukkit.getWorld(this.world), this.getX(), this.getY(), this.getZ());
    }

    /**
     * Gets a NetworkLocation based on the given org.bukkit.Location
     * @param location The location
     * @param serverName Name of the server of the location
     * @return NetworkLocation
     */
    public static NetworkLocation fromBukkitLocation(Location location, String serverName) {
        if (location.getWorld() == null)
            return null;

        return new NetworkLocation(serverName, location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    /**
     * Gets the name of the server of this location
     * @return name of the server
     */
    public String getServer() {
        return server;
    }

    /**
     * Gets the name of the world the location is in
     * @return name of the world
     */
    public String getWorld() {
        return world;
    }

    /**
     * Gets the x-coordinate of this location
     * @return x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of this location
     * @return y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the z-coordinate of this location
     * @return z-coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the name of the server of this location
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Sets the name of the world the location is in
     */
    public void setWorld(String world) {
        this.world = world;
    }

    /**
     * Sets the x-coordinate of this location
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate of this location
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Sets the z-coordinate of this location
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Gets a string representing this NetworkLocation
     */
    public String toString() {
        return "NetworkLocation{server=" + server + ", world=" + world + ", x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
