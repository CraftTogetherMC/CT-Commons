package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class MessagingServer extends Thread {
    private final String host;
    private final int port;
    private final String secretKey;
    private boolean listen;
    private ServerSocket serverSocket;
    private ArrayList<MessagingClient> clients;

    protected MessagingServer(String host, int port, String secretKey) {
        this.setName(CTCommons.getPluginInformation().getName() + " network thread");
        this.host = host;
        this.port = port;
        this.secretKey = secretKey;
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

                MessagingClient client = new MessagingClient(connection, secretKey);
                CTCommons.debug("[MessagingServer]: " + Arrays.toString(connection.getInetAddress().getAddress()) + " connected.", false);

                // Should we accept remote connections?
                boolean acceptRemote = CTCommons.plugin.getConfig().getBoolean("Messaging.Server.AcceptRemoteConnections");
                if (!acceptRemote && !client.getAddress().equals("127.0.0.1"))
                    client.kick(MessagingClient.Error.NO_REMOTE_CONNECTIONS);

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

        for (MessagingClient client : clients)
            client.disconnect();

        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}