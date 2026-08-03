package de.crafttogether.common.messaging.packets;

import java.util.List;

public class AuthenticationSuccessPacket extends Packet {
    private final List<String> serverList;
    public AuthenticationSuccessPacket(List<String> serverList) {
        this.serverList = serverList;
    }

    public List<String> getServerList() {
        return serverList;
    }
}

