package de.crafttogether.common.commands.platform.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.crafttogether.common.commands.AbstractCommandSender;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class VelocityCommandSender extends AbstractCommandSender<CommandSource> {
    private final Audience audience;
    private final CommandSource sender;

    public VelocityCommandSender(CommandSource sender) {
        super(sender);
        this.sender = sender;
        this.audience = Audience.audience(sender);
    }

    @Override
    public String getName() {
        if (super.delegate instanceof Player) {
            return ((Player) super.delegate).getUsername();
        }
        return null;
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

    public CommandSource getSender() {
        return sender;
    }
}