package de.crafttogether.common.platform.bukkit;

import de.crafttogether.common.commands.AbstractCommandSender;
import de.crafttogether.ctcommons.CTCommonsBukkit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitCommandSender extends AbstractCommandSender<CommandSender> {
    private final Audience audience;

    public BukkitCommandSender(CommandSender sender) {
        super(sender);
        this.audience = CTCommonsBukkit.audiences.sender(sender);
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
        return super.delegate;
    }
}