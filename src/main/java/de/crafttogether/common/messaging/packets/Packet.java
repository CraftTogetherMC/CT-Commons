package de.crafttogether.common.messaging.packets;

import java.util.ArrayList;
import java.util.List;

public abstract class Packet implements AbstractPacket {
    private Boolean broadcast = false;
    private String sender;
    private List<String> recipients = new ArrayList<>();

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public AbstractPacket setSender(String sender) {
        this.sender = sender;
        return this;
    }

    @Override
    public List<String> getRecipients() {
        return recipients;
    }

    @Override
    public AbstractPacket setRecipients(List<String> recipients) {
        this.recipients = recipients;
        return this;
    }

    @Override
    public AbstractPacket setRecipient(String recipient) {
        this.recipients = new ArrayList<>();
        this.recipients.add(recipient);
        return this;
    }

    @Override
    public AbstractPacket addRecipient(String recipient) {
        if (!this.recipients.contains(recipient))
            this.recipients.add(recipient);
        return this;
    }

    @Override
    public AbstractPacket removeRecipient(String recipient) {
        this.recipients.remove(recipient);
        return this;
    }

    @Override
    public AbstractPacket setBroadcast(Boolean broadcast) {
        this.broadcast = broadcast;
        return this;
    }

    @Override
    public Boolean getBroadcast() {
        return broadcast;
    }
}
