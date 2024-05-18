package de.crafttogether.common.messaging.packets;

@SuppressWarnings("unused")
public record AuthenticationPacket(String clientName, String key) implements Packet { }
