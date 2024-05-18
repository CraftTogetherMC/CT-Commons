package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;
import de.crafttogether.common.messaging.packets.Packet;

import java.util.List;

public class MessagingService {
    private static boolean enabled;
    private static MessagingServer messagingServer;
    private static MessagingClient messagingClient;

    private static AbstractConnection clientConnection;

    private MessagingService() { }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void enable() {
        if (enabled)
            return;

        String serverName;
        String host;
        int port;
        String secretKey;
        boolean acceptRemoteConnections;

        if (CTCommons.isProxy()) {
            host = CTCommons.plugin.getConfig().getString("Messaging.Server.BindAddress");
            port = CTCommons.plugin.getConfig().getInt("Messaging.Server.Port");
            secretKey = CTCommons.plugin.getConfig().getString("Messaging.Server.SecretKey");
            acceptRemoteConnections = CTCommons.plugin.getConfig().getBoolean("Messaging.Server.AcceptRemoteConnections");
            messagingServer = new MessagingServer(host, port, secretKey, acceptRemoteConnections);
        }
        else {
            host = CTCommons.plugin.getConfig().getString("Messaging.Connection.Host");
            port = CTCommons.plugin.getConfig().getInt("Messaging.Connection.Port");
            secretKey = CTCommons.plugin.getConfig().getString("Messaging.Connection.SecretKey");
            serverName = CTCommons.plugin.getConfig().getString("Messaging.ServerName");
            messagingClient = new MessagingClient(host, port, secretKey, serverName);
        }

        enabled = true;
    }

    public void toServer(String serverName, Packet packet) {
        forward(packet.setRecipient(serverName));
    }

    public void toServer(List<String> serverNames, Packet packet) {
        forward(packet.setRecipients(serverNames));
    }

    public void toProxy(Packet packet) {
        if (CTCommons.isProxy()) // TODO: Exception?
            return;

        forward(packet.setRecipient("proxy"));
    }

    public void broadcast(Packet packet) {
        forward(packet.setBroadcast(true));
    }

    private void forward(Packet packet) {
        if (CTCommons.isProxy())
            messagingServer.send(packet);
        else
            messagingClient.getClientConnection().send(packet);
    }

    public static void disable() {
        if (messagingClient != null)
            MessagingClient.closeAll();

        if (messagingServer != null)
            messagingServer.close();

        enabled = false;
    }
}
