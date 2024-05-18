package de.crafttogether.common.messaging.events;

import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.ConnectionError;

public class ConnectionErrorEvent implements Event {

    private final ConnectionError error;
    private final String targetHost;
    private final int targetPort;

    public ConnectionErrorEvent(ConnectionError error, String targetHost, int targetPort) {
        this.error = error;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public ConnectionError getError() {
        return error;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }
}
