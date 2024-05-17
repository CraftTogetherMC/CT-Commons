package de.crafttogether.common.messaging.packets;

@SuppressWarnings("unused")
public record AuthenticationPacket(String sender, String key) implements Packet { }
