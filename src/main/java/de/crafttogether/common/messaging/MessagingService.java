package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;
import de.crafttogether.common.messaging.packets.AbstractPacket;
import de.crafttogether.common.messaging.packets.PacketImplementationPacket;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.util.CommonUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MessagingService {
    private static boolean enabled;
    private static MessagingService instance;
    private static MessagingServer messagingServer;
    private static MessagingClient messagingClient;

    public MessagingService() {
        if (instance == null) {
            instance = this;
        } else {
            throw new IllegalStateException("MessagingService already constructed!");
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public void enable() {
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

    public static boolean toServer(String serverName, AbstractPacket packet) {
        return send(packet.setRecipient(serverName));
    }

    public static boolean toServer(List<String> serverNames, AbstractPacket packet) {
        return send(packet.setRecipients(serverNames));
    }

    public static void toProxy(AbstractPacket packet) {
        if (CTCommons.isProxy()) // TODO: Exception?
            return;

        send(packet.setRecipient("proxy"));
    }

    public static boolean broadcast(AbstractPacket packet) {
        return send(packet.setBroadcast(true));
    }

    private static boolean send(AbstractPacket packet) {
        if (!isEnabled())
            return false; // TODO: Not enabled exception?

        if (CTCommons.isProxy()) {
            messagingServer.send(
                    packet.setSender("proxy"));
            return true; // TODO: FIX THIS SHIT SOMEHOW
        }
        else
            return messagingClient.getClientConnection().send(
                    packet.setSender(messagingClient.getClientConnection().getClientName()));
    }

    public static void registerPacket(Class<?> packetClass) {
        if (CTCommons.isProxy())
            return;

        String className = packetClass.getName();
        String classAsPath = className.replace('.', '/') + ".class";
        InputStream stream = packetClass.getClassLoader().getResourceAsStream(classAsPath);

        send(new PacketImplementationPacket(packetClass.getName())
                .setSender(getServerName()));
        try {
            assert stream != null;
            byte[] classData = IOUtils.toByteArray(stream);
            messagingClient.getClientConnection().getObjOutputStream().writeObject(classData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getServerName() {
        return CTCommons.isProxy() ? "proxy" : CTCommons.plugin.getConfig().getString("Messaging.ServerName");
    }

    public static List<String> getConnectedServers() {
        if (CTCommons.isProxy())
            return messagingServer.getServerList();
        else
            return messagingClient.getServerList();
    }

    public void disable() {
        if (messagingClient != null)
            MessagingClient.closeAll();

        if (messagingServer != null)
            messagingServer.close();

        enabled = false;
    }
}
