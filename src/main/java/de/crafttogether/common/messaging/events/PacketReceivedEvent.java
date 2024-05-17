package de.crafttogether.common.messaging.events;

import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.packets.Packet;

import java.net.Socket;

public class PacketReceivedEvent implements Event {
    private final Socket connection;
    private final Packet packet;

    public PacketReceivedEvent(Socket connection, Packet packet) {
        this.connection = connection;
        this.packet = packet;
    }

    public Socket getConnection() {
        return connection;
    }

    public Packet getPacket() {
        return packet;
    }
}
