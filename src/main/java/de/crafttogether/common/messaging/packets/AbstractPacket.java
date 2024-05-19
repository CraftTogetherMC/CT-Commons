package de.crafttogether.common.messaging.packets;

import java.io.Serializable;
import java.util.List;

public interface AbstractPacket extends Serializable {
    String getSender();

    AbstractPacket setSender(String sender);

    List<String> getRecipients();

    AbstractPacket setRecipients(List<String> recipients);

    AbstractPacket setRecipient(String recipient);

    AbstractPacket addRecipient(String recipient);

    AbstractPacket removeRecipient(String recipient);

    AbstractPacket setBroadcast(Boolean broadcast);

    Boolean getBroadcast();
}
