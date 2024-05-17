package de.crafttogether.common.event;

import de.crafttogether.common.commands.CommandSender;

import java.net.InetAddress;

public interface Player extends CommandSender {
    InetAddress getAddress();
}
