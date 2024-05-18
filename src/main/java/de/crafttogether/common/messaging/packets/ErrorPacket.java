package de.crafttogether.common.messaging.packets;

import de.crafttogether.common.messaging.ConnectionState;

public class ErrorPacket extends Packet {
    private final ConnectionState error;

    public ErrorPacket(ConnectionState error) {
        this.error = error;
    }

    public ConnectionState getError() {
        return error;
    }
}
