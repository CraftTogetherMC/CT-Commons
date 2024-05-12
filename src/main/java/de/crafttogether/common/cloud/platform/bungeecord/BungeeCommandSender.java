package de.crafttogether.common.cloud.platform.bungeecord;

import de.crafttogether.common.cloud.AbstractCommandSender;
import de.crafttogether.ctcommons.CTCommonsBungee;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeCommandSender extends AbstractCommandSender<CommandSender> {
    private final Audience audience;
    private final CommandSender sender;

    public BungeeCommandSender(CommandSender sender) {
        super(sender);
        this.sender = sender;
        this.audience = CTCommonsBungee.adventure.sender(sender);
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public UUID getUniqueId() {
        if (super.delegate instanceof ProxiedPlayer) {
            return ((ProxiedPlayer) super.delegate).getUniqueId();
        }
        return null;
    }

    @Override
    public void sendMessage(Component message) {
        this.audience.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return super.delegate.hasPermission(permission);
    }

    public CommandSender getSender() {
        return sender;
    }
}