package de.crafttogether.common.messaging.events;

import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.MessagingClient;

public class ConnectionErrorEvent implements Event {

    private final MessagingClient.Error error;
    private final String targetHost;
    private final int targetPort;

    public ConnectionErrorEvent(MessagingClient.Error error, String targetHost, int targetPort) {
        this.error = error;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public MessagingClient.Error getError() {
        return error;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }
}
