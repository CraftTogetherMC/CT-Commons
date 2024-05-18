package de.crafttogether.common.messaging.events;

import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.Error;

public class ConnectionErrorEvent implements Event {

    private final Error error;
    private final String targetHost;
    private final int targetPort;

    public ConnectionErrorEvent(Error error, String targetHost, int targetPort) {
        this.error = error;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public Error getError() {
        return error;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }
}
