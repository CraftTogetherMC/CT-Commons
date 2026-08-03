package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.Event;
import de.crafttogether.common.messaging.events.ConnectionErrorEvent;
import de.crafttogether.common.messaging.packets.AbstractPacket;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static de.crafttogether.common.messaging.ConnectionState.CONNECTION_REFUSED;


public abstract class AbstractConnection extends Thread {
    private String clientName;
    private boolean authenticated;
    private boolean disconnectCalled;
    private Socket connection;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean connectionReady;
    private ObjectOutputStream objOutputStream;
    private CustomObjectInputStream objInputStream;

    protected AbstractConnection(Socket connection) {
        this.setName(CTCommons.getPluginInformation().getName() + " network thread");
        this.connection = connection;
        this.disconnectCalled = false;
        this.authenticated = false;
        this.connectionReady = false;

        try {
            this.connection.setSoTimeout(5000);

            outputStream = this.connection.getOutputStream();
            objOutputStream = new ObjectOutputStream(outputStream);
            objOutputStream.flush();

            PushbackInputStream pushbackInputStream = new PushbackInputStream(this.connection.getInputStream(), 4);
            byte[] streamHeader = pushbackInputStream.readNBytes(4);

            if (streamHeader.length != 4
                    || (streamHeader[0] & 0xFF) != 0xAC
                    || (streamHeader[1] & 0xFF) != 0xED
                    || (streamHeader[2] & 0xFF) != 0x00
                    || (streamHeader[3] & 0xFF) != 0x05) {
                throw new StreamCorruptedException("Invalid Java serialization stream header from "
                        + connection.getInetAddress().getHostAddress());
            }

            pushbackInputStream.unread(streamHeader);
            inputStream = pushbackInputStream;
            objInputStream = new CustomObjectInputStream(inputStream, new CustomClassLoader());

            this.connection.setSoTimeout(0);
            this.connectionReady = true;
        }

        catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                CTCommons.debug("[MessagingClient]: Connection refused.", false);
                Event event = new ConnectionErrorEvent(CONNECTION_REFUSED, getAddress(), getPort());
                CTCommons.getRunnableFactory().create(() -> CTCommons.getEventManager().callEvent(event)).runTask();
            }
        }
        catch (StreamCorruptedException ex) {
            CTCommons.debug("[MessagingServer]: Rejected invalid connection from "
                    + connection.getInetAddress().getHostAddress() + ": " + ex.getMessage(), false);
            closeSocketQuietly();
        }
        catch (SocketTimeoutException ex) {
            CTCommons.debug("[MessagingServer]: Handshake timeout from "
                    + connection.getInetAddress().getHostAddress() + ".", false);
            closeSocketQuietly();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            closeSocketQuietly();
        }

        if (connectionReady) {
            onConnection();
            start();
        }
    }

    private void closeSocketQuietly() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (IOException ignored) { }
    }

    public boolean isConnectionReady() {
        return connectionReady;
    }

    @Override
    public void run() {
        try {
            Object inputPacket;
            while (objInputStream != null && (inputPacket = objInputStream.readObject()) != null)
                onPacketReceived((AbstractPacket) inputPacket);
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
    private final Object sendLock = new Object();

    protected boolean send(AbstractPacket packet) {
        if (connection == null || !connection.isConnected() || connection.isClosed())
            return false;

        if (packet.getSender() == null || packet.getSender().isEmpty()) {
            CTCommons.getLogger().warn("[MessagingClient]: Unable to send message without specified sender #" + packet.getClass().getSimpleName());
            return false;
        }

        try {
            synchronized (sendLock) {
                objOutputStream.reset();
                objOutputStream.writeObject(packet);
                objOutputStream.flush();
            }
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
                objOutputStream  = null;
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
    public void onPacketReceived(AbstractPacket packet) { }
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

    public ObjectOutputStream getObjOutputStream() {
        return objOutputStream;
    }

    public CustomObjectInputStream getObjInputStream() {
        return objInputStream;
    }
}