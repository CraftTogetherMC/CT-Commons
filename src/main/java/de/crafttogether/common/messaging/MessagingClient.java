package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.events.ConnectionErrorEvent;
import de.crafttogether.common.messaging.events.PacketReceivedEvent;
import de.crafttogether.common.messaging.packets.AuthenticationPacket;
import de.crafttogether.common.messaging.packets.MessagePacket;
import de.crafttogether.common.messaging.packets.Packet;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MessagingClient extends Thread {
    public static final Collection<MessagingClient> activeClients = new ArrayList<>();

    private String clientName;
    private String secretKey;
    private Socket connection;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream objOutputStream;
    private ObjectInputStream objInputStream;

    public enum Error {
        CONNECTION_REFUSED, INVALID_AUTHENTICATION, NOT_AUTHENTICATED, NO_REMOTE_CONNECTIONS
    }

    protected MessagingClient(Socket connection, String secretKey) {
        this.secretKey = secretKey;
        this.connection = connection;

        try {
            outputStream = connection.getOutputStream();
            inputStream = connection.getInputStream();
            objOutputStream = new ObjectOutputStream(outputStream);
            objInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected MessagingClient(String host, int port, String secretKey) {
        this.secretKey = secretKey;
        this.setName(CTCommons.getPluginInformation().getName() + " network thread");

        CTCommons.debug("[MessagingClient]: Connecting to " + host + ":" + port + "...", false);

        try {
            connection = new Socket(host, port);
            outputStream = connection.getOutputStream();
            inputStream = connection.getInputStream();
            objOutputStream = new ObjectOutputStream(outputStream);
            objInputStream = new ObjectInputStream(inputStream);
        }

        catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                CTCommons.debug("[MessagingClient]: Connection refused.", false);
                Event event = new ConnectionErrorEvent(Error.CONNECTION_REFUSED, host, port);
                CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        if (connection != null && connection.isConnected() && objOutputStream != null) {
            CTCommons.debug("[MessagingClient]: Connection established.", false);

            // Start reader
            start();

            // Authenticate
            send(new AuthenticationPacket(CTCommons.plugin.getConfig().getString("Messaging.ServerName"), secretKey));
        }
    }

    @Override
    public void run() {
        read();
    }

    public void read() {
        activeClients.add(this);

        try {
            Object inputPacket;
            boolean authenticated = false;

            while (objInputStream != null && (inputPacket = objInputStream.readObject()) != null) {

                // First packet has to be an AuthenticationPacket
                if (!authenticated && inputPacket instanceof AuthenticationPacket packet) {
                    if (packet.key().equals(CTCommons.plugin.getConfig().getString("Portals.Server.SecretKey"))) {
                        setClientName(packet.sender());
                        authenticated = true;
                        CTCommons.debug("[MessagingClient]: Client (" + clientName + ") sucessfully authenticated.", false);
                    }
                    else
                        kick(Error.INVALID_AUTHENTICATION);
                }

                else if (!authenticated && inputPacket instanceof MessagePacket packet) {
                    if (packet.message().startsWith("ERROR:")) {
                        String message = packet.message().replace("ERROR:", "");
                        Error error = null;

                        try {
                            error = Error.valueOf(message);
                        } catch (IllegalArgumentException e) {
                            CTCommons.debug("Unkown error occured: " + e.getMessage());
                        }

                        Event event = new ConnectionErrorEvent(error, getAddress(), connection.getPort());
                        CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
                    }
                    else
                        CTCommons.debug("Received Message: " + packet.message());

                    disconnect();
                }

                else if (authenticated) {
                    CTCommons.debug(inputPacket.getClass().getName());
                    Event event = new PacketReceivedEvent(connection, (Packet) inputPacket);
                    CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
                }

                else
                    kick(Error.NOT_AUTHENTICATED);
            }
        }

        catch (EOFException ignored) { }

        catch (SocketException ex) {
            if ("Socket closed".equals(ex.getMessage())) {
                CTCommons.debug("[TCPClient]: Connection to " + connection.getInetAddress().getHostAddress() + " was closed.", false);
            } else {
                CTCommons.debug("[TCPClient]: " + ex.getMessage());
            }
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

        finally {
            CTCommons.debug("[TCPClient]: Closing connection to " + connection.getInetAddress().getHostAddress(), false);
            disconnect();
        }
    }

    public boolean send(Packet packet) {
        if (connection == null || !connection.isConnected() || connection.isClosed())
            return false;

        try {
            objOutputStream.reset();
            objOutputStream.writeObject(packet);
            objOutputStream.flush();
        }
        catch (SocketException e) {
            CTCommons.debug(e.getMessage());
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean send(String message) {
        return send(new MessagePacket(clientName, message));
    }

    public void kick(Error error) {
        clientName = clientName == null ? Arrays.toString(connection.getInetAddress().getAddress()) : clientName;
        CTCommons.debug("[MessagingServer]: " + clientName + " was kicked (" + error + ").", false);
        send("ERROR:" + error.name());
        disconnect();
    }

    public void disconnect() {
        try {
            if (objInputStream != null) {
                objInputStream.close();
                objInputStream = null;
            }

            if (objOutputStream != null) {
                objOutputStream.close();
                objInputStream = null;
            }

            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }

            activeClients.remove(this);
        } catch (Exception ex) {
            CTCommons.debug(ex.getMessage());
        }
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAddress() {
        return this.connection.getInetAddress().getHostAddress();
    }

    public static void closeAll() {
        int stopped = 0;

        for (MessagingClient client : activeClients) {
            client.disconnect();
            stopped++;
        }

        CTCommons.debug("[TCPClient]: Stopped " + stopped + " active clients.", false);
    }

    public void sendAuth(String secretKey) {
        AuthenticationPacket packet = new AuthenticationPacket(clientName, secretKey);
        send(packet);
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}