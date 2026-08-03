package de.crafttogether.common.platform.bukkit;

import de.crafttogether.common.event.AbstractPlayer;
import de.crafttogether.ctcommons.CTCommonsBukkit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.Objects;
import java.util.UUID;

public class BukkitPlayer extends AbstractPlayer<Player> {

    private final Audience audience;

    public BukkitPlayer(Player player) {
        super(player);
        this.audience = CTCommonsBukkit.audiences.player(player);
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public UUID getUniqueId() {
        return super.delegate.getUniqueId();
    }

    @Override
    public void sendMessage(Component message) {
        this.audience.sendMessage(message);
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
        return Objects.requireNonNull(super.delegate.getAddress()).getAddress();
    }
}
