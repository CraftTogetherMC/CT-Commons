package de.crafttogether.common.messaging.packets;

public class ServerDisconnectedPacket extends Packet {
    private final String serverName;

    public ServerDisconnectedPacket(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }
}
