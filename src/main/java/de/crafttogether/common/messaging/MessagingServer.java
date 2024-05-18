package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.events.ConnectionErrorEvent;
import de.crafttogether.common.messaging.events.PacketReceivedEvent;
import de.crafttogether.common.messaging.packets.AuthenticationPacket;
import de.crafttogether.common.messaging.packets.MessagePacket;
import de.crafttogether.common.messaging.packets.Packet;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

import static de.crafttogether.common.messaging.Error.NO_REMOTE_CONNECTIONS;

public class MessagingServer extends Thread {
    private static String host;
    private static int port;
    private static String secretKey;
    private static boolean acceptRemoteConnections;

    private boolean listen;
    private ServerSocket serverSocket;
    private static ArrayList<ClientConnection> clients;

    protected MessagingServer(String host, int port, String secretKey, boolean acceptRemoteConnections) {
        this.setName(CTCommons.getPluginInformation().getName() + " network thread");
        MessagingServer.host = host;
        MessagingServer.port = port;
        MessagingServer.secretKey = secretKey;
        MessagingServer.acceptRemoteConnections = acceptRemoteConnections;
        start();
    }

    @Override
    public void run() {
        clients = new ArrayList<>();

        try {
            // Create ServerSocket
            serverSocket = new ServerSocket(port, 5, InetAddress.getByName(host));
            listen = true;

            CTCommons.debug("[MessagingServer]: Server is listening on port " + port, false);

            // Handle incoming connections
            while (listen && !isInterrupted()) {
                Socket connection = null;

                try {
                    connection = serverSocket.accept();
                } catch (SocketException e) {
                    CTCommons.debug(e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (connection == null)
                    continue;

                ClientConnection client = new ClientConnection(connection);
                CTCommons.debug("[MessagingServer]: " + Arrays.toString(connection.getInetAddress().getAddress()) + " connected.", false);

                // Should we accept remote connections?
                if (!acceptRemoteConnections && !client.getAddress().equals("127.0.0.1"))
                    client.kick(NO_REMOTE_CONNECTIONS);

                clients.add(client);
                CTCommons.debug("[MessagingServer]: Starting network-thread...", false);
                client.read();
            }
        } catch (BindException e) {
            CTCommons.getLogger().warn("[MessagingServer]: Can't bind to " + port + ".. Port already in use!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        finally {
            close();
            CTCommons.debug("[MessagingServer]: Server stopped.", false);
        }
    }

    public void close() {
        if (!listen) return;
        listen = false;

        for (ClientConnection client : clients)
            client.disconnect();

        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    protected static class ClientConnection extends AbstractConnection {
        protected ClientConnection(Socket connection) {
            super(connection);
        }

        @Override
        public void onPacketReceived(Packet abstractPacket) {
            // First packet has to be an AuthenticationPacket
            if (!isAuthenticated() && abstractPacket instanceof AuthenticationPacket packet) {
                if (packet.key().equals(secretKey)) {
                    setClientName(packet.sender());
                    isAuthenticated(true);
                    CTCommons.debug("[MessagingClient]: Client (" + getClientName() + ") sucessfully authenticated.", false);
                }
                else
                    kick(Error.INVALID_AUTHENTICATION);
            }

            else if (!isAuthenticated() && abstractPacket instanceof MessagePacket packet) {
                if (packet.message().startsWith("ERROR:")) {
                    Error error = parseError(packet.message());

                    if (error != null) {
                        Event event = new ConnectionErrorEvent(error, getAddress(), getPort());
                        CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
                    }
                }
                else
                    CTCommons.debug("Received Message: " + packet.message());
            }

            else if (isAuthenticated()) {
                CTCommons.debug(abstractPacket.getClass().getName());
                Event event = new PacketReceivedEvent(getConnection(), abstractPacket);
                CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
            }

            else
                kick(Error.NOT_AUTHENTICATED);
        }

        @Override
        public void onDisconnect() {
            clients.remove(this);
        }

        public void kick(Error error) {
            CTCommons.debug("[MessagingServer]: " + getClientName() + " was kicked (" + error + ").", false);
            send("ERROR:" + error.name());
            disconnect();
        }
    }
}