package de.crafttogether.common.platform.velocity;

import com.velocitypowered.api.proxy.Player;
import de.crafttogether.common.event.AbstractPlayer;
import de.crafttogether.ctcommons.CTCommonsBungee;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.net.InetAddress;
import java.util.UUID;

public class VelocityPlayer extends AbstractPlayer<Player> {

    public VelocityPlayer(Player player) {
        super(player);
    }

    @Override
    public String getName() {
        return this.delegate.getUsername();
    }

    @Override
    public UUID getUniqueId() {
        return super.delegate.getUniqueId();
    }

    @Override
    public void sendMessage(Component message) {
        this.delegate.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return super.delegate.hasPermission(permission);
    }

    public Player getPlayer() {
        return super.delegate;
    }

    @Override
    public InetAddress getAddress() {
        return super.delegate.getRemoteAddress().getAddress();
    }
}
