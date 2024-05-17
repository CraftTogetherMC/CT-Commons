package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;

public class MessagingService {
    private static boolean enabled;
    private static MessagingServer messagingServer;
    private static MessagingClient messagingClient;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void enable() {
        if (enabled)
            return;

        String secretKey;
        int port;
        String host;

        if (CTCommons.isProxy()) {
            host = CTCommons.plugin.getConfig().getString("Messaging.Server.BindAddress");
            port = CTCommons.plugin.getConfig().getInt("Messaging.Server.Port");
            secretKey = CTCommons.plugin.getConfig().getString("Messaging.Server.SecretKey");
            messagingServer = new MessagingServer(host, port, secretKey);
        }
        else {
            host = CTCommons.plugin.getConfig().getString("Messaging.Connection.Host");
            port = CTCommons.plugin.getConfig().getInt("Messaging.Connection.Port");
            secretKey = CTCommons.plugin.getConfig().getString("Messaging.Connection.SecretKey");
            messagingClient = new MessagingClient(host, port, secretKey);
        }

        enabled = true;
    }

    public static void disable() {
        if (messagingClient != null)
            messagingClient.disconnect();

        if (messagingServer != null)
            messagingServer.close();

        enabled = false;
    }
}
