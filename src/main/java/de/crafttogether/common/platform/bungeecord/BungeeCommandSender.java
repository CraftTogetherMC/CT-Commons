package de.crafttogether.common.platform.bungeecord;

import de.crafttogether.common.commands.AbstractCommandSender;
import de.crafttogether.ctcommons.CTCommonsBungee;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeCommandSender extends AbstractCommandSender<CommandSender> {
    private final Audience audience;

    public BungeeCommandSender(CommandSender sender) {
        super(sender);
        this.audience = CTCommonsBungee.audiences.sender(sender);
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
        return super.delegate;
    }
}