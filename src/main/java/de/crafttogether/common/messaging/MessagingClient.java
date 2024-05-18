package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.events.ConnectionErrorEvent;
import de.crafttogether.common.messaging.events.PacketReceivedEvent;
import de.crafttogether.common.messaging.packets.*;
import de.crafttogether.ctcommons.CTCommonsCore;

import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

public class MessagingClient {
    private static MessagingClient instance;

    private static String host;
    private static int port;
    private static String secretKey;
    private static String serverName;
    private static ClientConnection clientConnection;
    private static final Collection<ClientConnection> activeClients = new ArrayList<>();

    private int reconnectAttempts;

    protected MessagingClient(String host, int port, String secretKey, String serverName) {
        instance = this;
        reconnectAttempts = 0;
        MessagingClient.host = host;
        MessagingClient.port = port;
        MessagingClient.secretKey = secretKey;
        MessagingClient.serverName = serverName;
        connect();
    }

    private void connect() {
        reconnectAttempts = 0;

        Socket connection = null;

        try {
            connection = new Socket(host, port);
        }

        catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                CTCommons.debug("[MessagingClient]: Connection to " + host + " was refused.", false);
                Event event = new ConnectionErrorEvent(ConnectionState.CONNECTION_REFUSED, host, port);
                CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        if (connection != null && connection.isConnected()) {
            CTCommons.debug("[MessagingServer]: Starting network-thread...", false);
            clientConnection = new ClientConnection(connection);
            activeClients.add(clientConnection);
            clientConnection.start();
        }
        else
            reconnect();
    }

    public void reconnect() {
        long wait = 10L;

        if (reconnectAttempts > 10)
            wait = 20L;

        if (reconnectAttempts > 20)
            wait = 60L;

        CTCommons.getRunnableFactory().create(() -> {
            CTCommons.debug("[MessagingClient]: Try to reconnect...", false);
            connect();
            reconnectAttempts++;
        }).runTaskLaterAsynchronously(wait);
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    protected static class ClientConnection extends AbstractConnection {

        protected ClientConnection(Socket connection) {
            super(connection);
        }

        @Override
        public void onConnection() {
            CTCommons.debug("[MessagingClient]: Client connected!", false);
            send(new AuthenticationPacket(serverName, secretKey)
                    .addRecipient("proxy")
                    .setSender(serverName));
        }

        @Override
        public void onPacketReceived(Packet abstractPacket) {
            if (abstractPacket instanceof ErrorPacket packet) {
                Event event = new ConnectionErrorEvent(packet.getError(), getAddress(), getPort());
                CTCommons.debug("[MessagingClient]: Error: " + packet.getError().name());
                CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
            }

            else if (abstractPacket instanceof AuthenticationSuccessPacket packet) {
                CTCommons.debug("[MessagingClient]: Client sucessfully authenticated!", false);
                isAuthenticated(true);
            }

            else {
                CTCommons.debug(abstractPacket.getClass().getSimpleName());
                Event event = new PacketReceivedEvent(getConnection(), abstractPacket);
                CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
            }
        }

        @Override
        public void onDisconnect(boolean forced) {
            CTCommons.debug("[MessagingClient]: Client disconnected.", false);

            if (!forced)
                instance.reconnect();
        }
    }

    public static void closeAll() {
        int stopped = 0;

        for (ClientConnection connection : activeClients) {
            connection.disconnect();
            stopped++;
        }

        CTCommons.debug("[MessagingClient]: Stopped " + stopped + " active clients.", false);
    }
}