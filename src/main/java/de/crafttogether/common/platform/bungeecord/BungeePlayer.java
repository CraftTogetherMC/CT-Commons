package de.crafttogether.common.platform.bungeecord;

import de.crafttogether.common.event.AbstractPlayer;
import de.crafttogether.ctcommons.CTCommonsBungee;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetAddress;
import java.util.UUID;

public class BungeePlayer extends AbstractPlayer<ProxiedPlayer> {

    private final Audience audience;

    public BungeePlayer(ProxiedPlayer player) {
        super(player);
        this.audience = CTCommonsBungee.audiences.player(player);
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

    public ProxiedPlayer getPlayer() {
        return super.delegate;
    }

    @Override
    public InetAddress getAddress() {
        return super.delegate.getAddress().getAddress();
    }
}
