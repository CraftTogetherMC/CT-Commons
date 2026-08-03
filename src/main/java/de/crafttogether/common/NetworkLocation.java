package de.crafttogether.common;

import java.io.Serializable;

/**
 * Platform-agnostic representation of a location in a proxy network.
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

    public String getServer() {
        return this.server;
    }

    public String getWorld() {
        return this.world;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }


    public double getZ() {
        return this.z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
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

    public void setYaw(float pitch) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String toString() {
        return "NetworkLocation{server=" + this.server + ", world=" + this.world + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", yaw=" + this.yaw + ", pitch=" + this.pitch + "}";
    }
}
