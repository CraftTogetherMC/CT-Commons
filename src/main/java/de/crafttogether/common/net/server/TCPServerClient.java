package de.crafttogether.common.net.server;

import de.crafttogether.common.net.packets.MessagePacket;
import de.crafttogether.common.net.packets.Packet;
import de.crafttogether.common.util.CommonUtil;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

class TCPServerClient {
    private Socket connection;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream objOutputStream;
    private ObjectInputStream objInputStream;

    TCPServerClient(Socket connection) {
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

    public void read() {
        try {
            Object inputPacket;

            while (objInputStream != null && (inputPacket = objInputStream.readObject()) != null) {
                // Receive
            }
        }

        catch (EOFException ignored) { }

        catch (SocketException ex) {
            if ("Socket closed".equals(ex.getMessage())) {
                CommonUtil.debug("[TCPClient]: Connection to " + connection.getInetAddress().getHostAddress() + " was closed.");
            } else {
                CommonUtil.debug("[TCPClient]: " + ex.getMessage());
            }
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

        finally {
            CommonUtil.debug("[TCPClient]: Closing connection to " + connection.getInetAddress().getHostAddress());
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

        } catch (Exception ex) {
            CommonUtil.debug(ex.getMessage());
        }
    }

    public String getAddress() {
        return this.connection.getInetAddress().getHostAddress();
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
