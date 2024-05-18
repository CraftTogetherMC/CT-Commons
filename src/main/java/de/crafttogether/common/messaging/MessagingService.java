package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;
import de.crafttogether.common.messaging.packets.Packet;

import java.util.List;

public class MessagingService {
    private static boolean enabled;
    private static MessagingServer messagingServer;
    private static MessagingClient messagingClient;

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

    public static void toServer(String serverName, Packet packet) {
        send(packet.setRecipient(serverName));
    }

    public static void toServer(List<String> serverNames, Packet packet) {
        send(packet.setRecipients(serverNames));
    }

    public static void toProxy(Packet packet) {
        if (CTCommons.isProxy()) // TODO: Exception?
            return;

        send(packet.setRecipient("proxy"));
    }

    public static void broadcast(Packet packet) {
        send(packet.setBroadcast(true));
    }

    private static void send(Packet packet) {
        if (!isEnabled())
            return; // TODO: Not enabled exception?

        if (CTCommons.isProxy())
            messagingServer.send(
                    packet.setSender("proxy"));
        else
            messagingClient.getClientConnection().send(
                    packet.setSender(messagingClient.getClientConnection().getClientName()));
    }

    public static List<String> getConnectedServers() {
        if (CTCommons.isProxy())
            return messagingServer.getServerList();
        else
            return messagingClient.getServerList();
    }

    public static void disable() {
        if (messagingClient != null)
            MessagingClient.closeAll();

        if (messagingServer != null)
            messagingServer.close();

        enabled = false;
    }
}
