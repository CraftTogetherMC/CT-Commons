package de.crafttogether.common.messaging.packets;

public class PacketImplementationPacket extends Packet {
    private final String className;

    public PacketImplementationPacket(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
