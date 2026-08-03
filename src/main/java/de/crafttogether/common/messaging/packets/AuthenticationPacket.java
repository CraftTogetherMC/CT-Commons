package de.crafttogether.common.messaging.packets;

public class AuthenticationPacket extends Packet {
    private final String clientName;
    private final String secretKey;

    public AuthenticationPacket(String clientName, String secretKey) {
        this.clientName = clientName;
        this.secretKey = secretKey;
    }

    public String getClientName() {
        return clientName;
    }

    public String getSecretKey() {
        return secretKey;
    }
}