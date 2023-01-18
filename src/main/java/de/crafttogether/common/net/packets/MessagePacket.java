package de.crafttogether.common.net.packets;

public class MessagePacket implements Packet {
    public final String message;

    public MessagePacket(String message) {
        this.message = message;
    }
}