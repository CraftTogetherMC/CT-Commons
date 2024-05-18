package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.events.ConnectionErrorEvent;
import de.crafttogether.common.messaging.packets.Packet;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

import static de.crafttogether.common.messaging.ConnectionState.CONNECTION_REFUSED;


public abstract class AbstractConnection extends Thread {
    private String clientName;
    private boolean authenticated;
    private boolean disconnectCalled;
    private Socket connection;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream objOutputStream;
    private ObjectInputStream objInputStream;

    protected AbstractConnection(Socket connection) {
        this.setName(CTCommons.getPluginInformation().getName() + " network thread");
        this.connection = connection;
        this.disconnectCalled = false;
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

    @Override
    public void run() {
        try {
            Object inputPacket;
            while (objInputStream != null && (inputPacket = objInputStream.readObject()) != null)
                onPacketReceived((Packet) inputPacket);
        }

        catch (EOFException ignored) { }

        catch (SocketException ex) {
            if ("Socket closed".equals(ex.getMessage())) {
                CTCommons.debug("SOCKET CLOSED");
            } else {
                CTCommons.debug("[MessagingClient]: " + ex.getMessage());
            }
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

        finally {
            CTCommons.debug("[MessagingClient]: Closed connection to " + getClientName() + ".", false);
            finalizeConnection();
        }

        try {
            this.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean send(Packet packet) {
        if (connection == null || !connection.isConnected() || connection.isClosed())
            return false;

        if (packet.getSender() == null || packet.getSender().isEmpty()) {
            CTCommons.getLogger().warn("[MessagingClient]: Unable to send message without specified sender #" + packet.getClass().getSimpleName());
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

        return true;
    }

    public void disconnect() {
        disconnectCalled = true;
        finalizeConnection();
    }

    public void finalizeConnection() {
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

            onDisconnect(disconnectCalled);
        } catch (Exception ex) {
            CTCommons.debug(ex.getMessage());
        }
    }

    public void onConnection() { }
    public void onPacketReceived(Packet packet) { }
    public void onDisconnect(boolean forced) { }

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
