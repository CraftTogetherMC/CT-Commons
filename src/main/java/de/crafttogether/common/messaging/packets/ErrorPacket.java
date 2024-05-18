package de.crafttogether.common.messaging.packets;

import de.crafttogether.common.messaging.ConnectionError;

public class ErrorPacket extends Packet {
    private final ConnectionError error;

    public ErrorPacket(ConnectionError error) {
        this.error = error;
    }

    public ConnectionError getError() {
        return error;
    }
}
