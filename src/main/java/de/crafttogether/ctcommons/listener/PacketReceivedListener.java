package de.crafttogether.ctcommons.listener;

import de.crafttogether.CTCommons;
import de.crafttogether.common.event.EventListener;
import de.crafttogether.common.event.Listener;
import de.crafttogether.common.messaging.events.PacketReceivedEvent;

public class PacketReceivedListener implements Listener {

    @EventListener
    public void onPacketReceived(final PacketReceivedEvent event) {
        CTCommons.getLogger().warn("RECEIVED PACKET " + event.getClass().getSimpleName());

    }
}
