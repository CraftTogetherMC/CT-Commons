package de.crafttogether.common;

import java.io.Serializable;

/**
 * Representation of a location in a BungeeCord network
 **/

@SuppressWarnings("unused")
public class NetworkLocation implements Serializable {
    private String server;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;


    public NetworkLocation(String server, String world, double x, double y, double z) {
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
        this.pitch = 0;
    }
    public NetworkLocation(String server, String world, double x, double y, double z, float yaw, float pitch) {
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Gets the corresponding org.bukkit.Location of this location
     * @return Location
     */
    public org.bukkit.Location getBukkitLocation() {
        if (org.bukkit.Bukkit.getWorld(this.world) == null)
            return null;

        return new org.bukkit.Location(org.bukkit.Bukkit.getWorld(this.world), this.getX(), this.getY(), this.getZ(), this.yaw, this.pitch);
    }

    /**
     * Gets a NetworkLocation based on the given org.bukkit.Location
     * @param location The location
     * @param serverName Name of the server of the location
     * @return NetworkLocation
     */
    public static NetworkLocation fromBukkitLocation(org.bukkit.Location location, String serverName) {
        if (location.getWorld() == null)
            return null;

        return new NetworkLocation(serverName, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Gets the name of the server of this location
     * @return name of the server
     */
    public String getServer() {
        return this.server;
    }

    /**
     * Gets the name of the world the location is in
     * @return name of the world
     */
    public String getWorld() {
        return this.world;
    }

    /**
     * Gets the x-coordinate of this location
     * @return x-coordinate
     */
    public double getX() {
        return this.x;
    }

    /**
     * Gets the y-coordinate of this location
     * @return y-coordinate
     */
    public double getY() {
        return this.y;
    }

    /**
     * Gets the z-coordinate of this location
     * @return z-coordinate
     */
    public double getZ() {
        return this.z;
    }
    /**
     * Gets the horizontal rotation of this location
     * @return yaw
     */
    public double getYaw() {
        return this.yaw;
    }
    /**
     * Gets the vertical rotation of this location
     * @return pitch
     */
    public double getPitch() {
        return this.pitch;
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
     * Sets the horizontal rotation of this location
     */
    public void setYaw(float pitch) {
        this.yaw = yaw;
    }

    /**
     * Sets the vertical rotation of this location
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Gets a string representing this NetworkLocation
     */
    public String toString() {
        return "NetworkLocation{server=" + this.server + ", world=" + this.world + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", yaw=" + this.yaw + ", pitch=" + this.pitch + "}";
    }
}
