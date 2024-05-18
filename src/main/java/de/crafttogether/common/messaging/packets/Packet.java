package de.crafttogether.common.messaging.packets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Packet implements Serializable {
    private Boolean broadcast = false;
    private String sender;
    private List<String> recipients = new ArrayList<>();

    public String getSender() {
        return sender;
    }

    public Packet setSender(String sender) {
        this.sender = sender;
        return this;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public Packet setRecipients(List<String> recipients) {
        this.recipients = recipients;
        return this;
    }

    public Packet setRecipient(String recipient) {
        this.recipients = new ArrayList<>();
        this.recipients.add(recipient);
        return this;
    }

    public Packet addRecipient(String recipient) {
        if (!this.recipients.contains(recipient))
            this.recipients.add(recipient);
        return this;
    }

    public Packet removeRecipient(String recipient) {
        this.recipients.remove(recipient);
        return this;
    }

    public Packet setBroadcast(Boolean broadcast) {
        this.broadcast = broadcast;
        return this;
    }

    public Boolean getBroadcast() {
        return broadcast;
    }
}
