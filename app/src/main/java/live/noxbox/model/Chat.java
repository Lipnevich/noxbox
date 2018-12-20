package live.noxbox.model;

import java.util.HashMap;
import java.util.Map;

public class Chat {
    private Map<String, Message> ownerMessages = new HashMap<>();
    private Map<String, Message> partyMessages = new HashMap<>();

    private Long ownerReadTime;
    private Long partyReadTime;

    public Map<String, Message> getOwnerMessages() {
        if (ownerMessages == null) {
            ownerMessages = new HashMap<>();
        }
        return ownerMessages;
    }

    public Chat setOwnerMessages(Map<String, Message> ownerMessages) {
        this.ownerMessages = ownerMessages;
        return this;
    }

    public Map<String, Message> getPartyMessages() {
        if (partyMessages == null) {
            partyMessages = new HashMap<>();
        }
        return partyMessages;
    }

    public Chat setPartyMessages(Map<String, Message> partyMessages) {
        this.partyMessages = partyMessages;
        return this;
    }

    public Long getOwnerReadTime() {
        if(ownerReadTime == null){
            ownerReadTime = 0L;
        }
        return ownerReadTime;
    }

    public Chat setOwnerReadTime(Long ownerReadTime) {
        this.ownerReadTime = ownerReadTime;
        return this;
    }

    public Long getPartyReadTime() {
        if(partyReadTime == null){
            partyReadTime = 0L;
        }
        return partyReadTime;
    }

    public Chat setPartyReadTime(Long partyReadTime) {
        this.partyReadTime = partyReadTime;
        return this;
    }
}
