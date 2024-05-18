package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.events.ConnectionErrorEvent;
import de.crafttogether.common.messaging.packets.Packet;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

import static de.crafttogether.common.messaging.ConnectionError.CONNECTION_REFUSED;


public abstract class AbstractConnection {
    private String clientName;
    private boolean authenticated;
    private Socket connection;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream objOutputStream;
    private ObjectInputStream objInputStream;

    protected AbstractConnection(Socket connection) {
        this.connection = connection;
        this.authenticated = false;

        try {
            outputStream = this.connection.getOutputStream();
            inputStream = this.connection.getInputStream();
            objOutputStream = new ObjectOutputStream(outputStream);
            objInputStream = new ObjectInputStream(inputStream);
        }

        catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                CTCommons.debug("[MessagingClient]: Connection refused.", false);
                Event event = new ConnectionErrorEvent(CONNECTION_REFUSED, getAddress(), getPort());
                CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        if (connection != null && connection.isConnected() && objOutputStream != null)
            onConnection();
    }

    public void read() {
        try {
            Object inputPacket;
            while (objInputStream != null && (inputPacket = objInputStream.readObject()) != null)
                onPacketReceived((Packet) inputPacket);
        }

        catch (EOFException ignored) { }

        catch (SocketException ex) {
            if ("Socket closed".equals(ex.getMessage())) {
                //CTCommons.debug("[MessagingClient]: Connection to " + getAddress() + " was closed.", false);
            } else {
                CTCommons.debug("[MessagingClient]: " + ex.getMessage());
            }
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

        finally {
            CTCommons.debug("[MessagingClient]: Closed connection to " + getAddress() + ".", false);
            disconnect();
        }
    }

    public boolean send(Packet packet) {
        if (connection == null || !connection.isConnected() || connection.isClosed())
            return false;

        if (packet.getSender() == null || packet.getSender().isEmpty()) {
            CTCommons.getLogger().warn("[MessagingClient]: Unable to send message without specified sender #" + packet.getClass().getSimpleName());
            return false;
        }

        if (packet.getRecipients() == null || packet.getRecipients().isEmpty()) {
            CTCommons.getLogger().warn("[MessagingClient]: Unable to send message without specified recipients #" + packet.getClass().getSimpleName() + " Sender: " + packet.getSender());
            return false;
        }

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

        CTCommons.getLogger().warn("[MessagingClient]: Sent #" + packet.getClass().getSimpleName() + " to " + packet.getRecipients() + " from " + packet.getSender());

        return true;
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

            onDisconnect();
        } catch (Exception ex) {
            CTCommons.debug(ex.getMessage());
        }
    }

    public void onConnection() {}
    public void onPacketReceived(Packet packet) { }
    public void onDisconnect() { }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void isAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Socket getConnection() {
        return connection;
    }

    public String getClientName() {
        return clientName == null ? getAddress() : clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAddress() {
        return this.connection.getInetAddress().getHostAddress();
    }
    public int getPort() {
        return this.connection.getPort();
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
