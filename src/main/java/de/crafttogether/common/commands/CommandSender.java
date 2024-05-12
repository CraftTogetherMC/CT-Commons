package de.crafttogether.common.commands;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface CommandSender {

    String getName();

    UUID getUniqueId();

    void sendMessage(Component message);

    boolean hasPermission(String permission);
}
