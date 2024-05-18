package de.crafttogether.common.messaging.packets;

public class ServerConnectedPacket extends Packet {
    private final String serverName;

    public ServerConnectedPacket(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }
}
