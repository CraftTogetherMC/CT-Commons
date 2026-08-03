package de.crafttogether.common.messaging.events;

import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.ConnectionState;

public class ConnectionErrorEvent implements Event {

    private final ConnectionState error;
    private final String targetHost;
    private final int targetPort;

    public ConnectionErrorEvent(ConnectionState error, String targetHost, int targetPort) {
        this.error = error;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public ConnectionState getError() {
        return error;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }
}
