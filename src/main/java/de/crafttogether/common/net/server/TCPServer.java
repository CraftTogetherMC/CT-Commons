package de.crafttogether.common.net.server;

import de.crafttogether.CTCommons;
import de.crafttogether.common.util.CommonUtil;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class TCPServer extends Thread {
    private final String host;
    private final int port;
    private boolean listen;
    private ServerSocket serverSocket;
    private ArrayList<TCPServerClient> clients;

    public TCPServer(String host, int port) {
        this.setName(CTCommons.plugin.getName() + " network thread");
        this.host = host;
        this.port = port;
        start();
    }

    @Override
    public void run() {
        clients = new ArrayList<>();

        try {
            // Create ServerSocket
            serverSocket = new ServerSocket(port, 5, InetAddress.getByName(host));
            listen = true;

            CommonUtil.debug("[TCPServer]: Server is listening on port " + port);

            // Handle incoming connections
            while (listen && !isInterrupted()) {
                Socket connection = null;

                try {
                    connection = serverSocket.accept();
                } catch (SocketException e) {
                    CommonUtil.debug(e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (connection == null)
                    continue;

                TCPServerClient client = new TCPServerClient(connection);
                CommonUtil.debug("[TCPServer]: " + client.getAddress() + " connected.");

                clients.add(client);
            }
        } catch (BindException e) {
            CTCommons.plugin.getLogger().warning("[TCPServer]: Can't bind to " + port + ".. Port already in use!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        finally {
            close();
            CommonUtil.debug("[TCPServer]: Server stopped.");
        }
    }

    public void close() {
        if (!listen) return;
        listen = false;

        for (TCPServerClient client : clients)
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