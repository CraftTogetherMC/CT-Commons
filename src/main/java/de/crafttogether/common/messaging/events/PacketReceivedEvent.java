package de.crafttogether.common.messaging.events;

import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.packets.AbstractPacket;

import java.net.Socket;

public class PacketReceivedEvent implements Event {
    private final Socket connection;
    private final AbstractPacket packet;

    public PacketReceivedEvent(Socket connection, AbstractPacket packet) {
        this.connection = connection;
        this.packet = packet;
    }

    public Socket getConnection() {
        return connection;
    }

    public AbstractPacket getPacket() {
        return packet;
    }
}
