package de.crafttogether.common.cloud.platform.bukkit;

import de.crafttogether.common.cloud.AbstractCommandSender;
import de.crafttogether.ctcommons.CTCommonsBukkit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitCommandSender extends AbstractCommandSender<CommandSender> {
    private final Audience audience;
    private final CommandSender sender;

    public BukkitCommandSender(CommandSender sender) {
        super(sender);
        this.sender = sender;
        this.audience = CTCommonsBukkit.adventure.sender(sender);
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public UUID getUniqueId() {
        if (super.delegate instanceof Player) {
            return ((Player) super.delegate).getUniqueId();
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