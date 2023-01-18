package de.crafttogether.common.net.client;

import de.crafttogether.CTCommons;
import de.crafttogether.common.net.packets.MessagePacket;
import de.crafttogether.common.net.packets.Packet;
import de.crafttogether.common.util.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class TCPClient extends Thread {
    public static final Collection<TCPClient> activeClients = new ArrayList<>();

    private Socket connection;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream objOutputStream;
    private ObjectInputStream objInputStream;

    public TCPClient(Socket connection) {
        this.connection = connection;

        try {
            outputStream = connection.getOutputStream();
            inputStream = connection.getInputStream();
            objOutputStream = new ObjectOutputStream(outputStream);
            objInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (connection.isConnected() && objOutputStream != null)
            read();
    }

    public TCPClient(String host, int port, UUID trainId) {
        this.setName(CTCommons.plugin.getName() + " network thread");

        try {
            connection = new Socket(host, port);
            outputStream = connection.getOutputStream();
            inputStream = connection.getInputStream();
            objOutputStream = new ObjectOutputStream(outputStream);
            objInputStream = new ObjectInputStream(inputStream);
        }

        catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                Event event = new ConnectionErrorEvent(Error.CONNECTION_REFUSED, trainId, host, port);
                Bukkit.getServer().getScheduler().runTask(CTCommons.plugin, () -> CommonUtil.callEvent(event));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        if (connection != null && connection.isConnected() && objOutputStream != null)
            start();
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

                // First packet has to be our secretKey
                if (!authenticated && inputPacket instanceof AuthenticationPacket packet) {
                    if (packet.key.equals(CTCommons.plugin.getConfig().getString("Portals.Server.SecretKey")))
                        authenticated = true;

                    else {
                        CommonUtil.debug("[TCPClient]: " + getAddress() + " has sent an invalid authentication");
                        send("ERROR:" + Error.INVALID_AUTHENTICATION.name());
                        disconnect();
                    }
                }

                else if (!authenticated && inputPacket instanceof MessagePacket packet) {
                    if (packet.message.startsWith("ERROR:")) {
                        String message = packet.message.replace("ERROR:", "");
                        Error error = null;

                        try {
                            error = Error.valueOf(message);
                        } catch (IllegalArgumentException e) {
                            CommonUtil.debug("Unkown error occured: " + e.getMessage());
                        }

                        Event event = new ConnectionErrorEvent(error, trainId, getAddress(), connection.getPort());
                        Bukkit.getServer().getScheduler().runTask(CTCommons.plugin, () -> CommonUtil.callEvent(event));
                    }
                    else
                        CommonUtil.debug("Received Message: " + packet.message);

                    disconnect();
                }

                else if (authenticated) {
                    CommonUtil.debug(inputPacket.getClass().getName());
                    Event event = new PacketReceivedEvent(connection, (Packet) inputPacket);
                    Bukkit.getServer().getScheduler().runTask(TCPortals.plugin, () -> CommonUtil.callEvent(event));
                }

                else {
                    CommonUtil.debug("[TCPClient]: " + getAddress() + " is not authenticated.");
                    send("ERROR:" + Error.NOT_AUTHENTICATED.name());
                    disconnect();
                }
            }
        }

        catch (EOFException ignored) { }

        catch (SocketException ex) {
            if ("Socket closed".equals(ex.getMessage())) {
                CommonUtil.debug("[TCPClient]: Connection to " + connection.getInetAddress().getHostAddress() + " was closed.", false);
            } else {
                CommonUtil.debug("[TCPClient]: " + ex.getMessage());
            }
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

        finally {
            CommonUtil.debug("[TCPClient]: Closing connection to " + connection.getInetAddress().getHostAddress(), false);
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
            CommonUtil.debug(e.getMessage());
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
        return send(new MessagePacket(message));
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
            CommonUtil.debug(ex.getMessage());
        }
    }

    public UUID getTrainId() {
        return trainId;
    }

    public String getAddress() {
        return this.connection.getInetAddress().getHostAddress();
    }

    public static void closeAll() {
        int stopped = 0;

        for (TCPClient client : activeClients) {
            client.disconnect();
            stopped++;
        }

        CommonUtil.debug("[TCPClient]: Stopped " + stopped + " active clients.");
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}