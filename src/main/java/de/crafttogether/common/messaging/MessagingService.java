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
    private static final java.util.Set<Class<?>> pendingPackets =
            java.util.concurrent.ConcurrentHashMap.newKeySet();
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

    public static boolean toProxy(AbstractPacket packet) {
        if (CTCommons.isProxy()) {
            return false;
        }

        return send(packet.setRecipient("proxy"));
    }

    public static boolean broadcast(AbstractPacket packet) {
        return send(packet.setBroadcast(true));
    }

    private static boolean send(AbstractPacket packet) {
        if (!isEnabled()) return false;

        if (CTCommons.isProxy()) {
            messagingServer.send(packet.setSender("proxy"));
            return true;
        }

        var conn = messagingClient.getClientConnection();
        if (conn == null) {
            // Noch nicht verbunden -> drop oder queue
            return false;
        }

        return conn.send(packet.setSender(conn.getClientName()));
    }

    public static void registerPacket(Class<?> packetClass) {
        if (CTCommons.isProxy()) return;

        pendingPackets.add(packetClass);
        flushPendingPackets();
    }

    public static void flushPendingPackets() {
        if (CTCommons.isProxy()) return;
        if (!isEnabled()) return;

        var conn = messagingClient.getClientConnection();
        if (conn == null) return;

        // optional: lock, damit send + writeObject nicht zwischen Threads vermischt
        synchronized (conn) {
            for (Class<?> c : pendingPackets) {
                sendPacketImplementation(conn, c);
            }
            pendingPackets.clear();
        }
    }

    private static void sendPacketImplementation(MessagingClient.ClientConnection conn, Class<?> packetClass) {
        String className = packetClass.getName();
        String classAsPath = className.replace('.', '/') + ".class";

        try (InputStream stream = packetClass.getClassLoader().getResourceAsStream(classAsPath)) {
            if (stream == null) {
                throw new IllegalStateException("Class bytes not found for " + className + " (" + classAsPath + ")");
            }

            // Einmal conn verwenden, nicht messagingClient.getClientConnection() mehrfach
            conn.send(new PacketImplementationPacket(className).setSender(getServerName()));

            byte[] classData = IOUtils.toByteArray(stream);
            conn.getObjOutputStream().writeObject(classData);
            conn.getObjOutputStream().flush();
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
