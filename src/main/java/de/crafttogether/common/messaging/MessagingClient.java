package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.events.ConnectionErrorEvent;
import de.crafttogether.common.messaging.events.PacketReceivedEvent;
import de.crafttogether.common.messaging.packets.AuthenticationPacket;
import de.crafttogether.common.messaging.packets.AuthenticationSuccess;
import de.crafttogether.common.messaging.packets.MessagePacket;
import de.crafttogether.common.messaging.packets.Packet;

import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

public class MessagingClient extends Thread {
    private static String host;
    private static int port;
    private static String secretKey;
    private static String serverName;

    private static ClientConnection clientConnection;

    private static final Collection<ClientConnection> activeClients = new ArrayList<>();

    protected MessagingClient(String host, int port, String secretKey, String serverName) {
        this.setName(CTCommons.getPluginInformation().getName() + " network thread");
        MessagingClient.host = host;
        MessagingClient.port = port;
        MessagingClient.secretKey = secretKey;
        MessagingClient.serverName = serverName;

        Socket connection = null;
        try {
            connection = new Socket(host, port);
        }

        catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                Event event = new ConnectionErrorEvent(Error.CONNECTION_REFUSED, host, port);
                CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        if (connection != null && connection.isConnected()) {
            clientConnection = new ClientConnection(connection);
            activeClients.add(clientConnection);
            start();
        }

    }

    @Override
    public void run() {
        clientConnection.read();
    }

    protected class ClientConnection extends AbstractConnection {

        protected ClientConnection(Socket connection) {
            super(connection);
        }

        @Override
        public void onConnection() {
            CTCommons.debug("[MessagingClient]: Client connected!", false);
            send(new AuthenticationPacket(serverName, secretKey));
        }

        @Override
        public void onPacketReceived(Packet abstractPacket) {
            if (abstractPacket instanceof MessagePacket packet) {
                if (packet.message().startsWith("ERROR:")) {
                    Error error = parseError(packet.message());

                    if (error != null) {
                        Event event = new ConnectionErrorEvent(error, getAddress(), getPort());
                        CTCommons.debug("[MessagingClient]: Error: " + error.name());
                        CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
                    }
                }
                else
                    CTCommons.debug("[MessagingClient]: Received Message: " + packet.message());
            }

            else if (abstractPacket instanceof AuthenticationSuccess packet) {
                CTCommons.debug("[MessagingClient]: Client sucessfully authenticated!", false);
                isAuthenticated(true);
            }

            else {
                CTCommons.debug(abstractPacket.getClass().getName());
                Event event = new PacketReceivedEvent(getConnection(), abstractPacket);
                CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
            }
        }

        @Override
        public void onDisconnect() {
            CTCommons.debug("[MessagingClient]: Client disconnected.", false);
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