package de.crafttogether.common.messaging.packets;

public class MessagePacket extends Packet {
    private final String message;

    public MessagePacket(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
