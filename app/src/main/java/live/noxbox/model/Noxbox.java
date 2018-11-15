/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package live.noxbox.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Noxbox implements Comparable<Noxbox> {

    private String id;
    @Virtual
    private String geoId;

    private Profile owner;
    private Profile party;
    private Map<String, Message> ownerMessages = new HashMap<>();
    private Map<String, Message> partyMessages = new HashMap<>();

    private Position position;
    private String price;
    private NoxboxType type;
    private MarketRole role;
    private WorkSchedule workSchedule;
    private String estimationTime;

    // Noxbox specific data
    private Long timeCreated;
    private Long timeRemoved;
    private Long timeRequested;
    private Long timeCompleted;
    private Long timeAccepted;
    private Long timeCanceledByOwner;
    private Long timeCanceledByParty;
    private Long timeOwnerVerified;
    private Long timePartyVerified;
    private Long timeOwnerDisliked;
    private Long timePartyDisliked;
    private Long timeToMeet;
    private Long timeTimeout;
    private String cancellationReasonMessage;
    private String commentForDemand;
    private String commentForSupply;

    public Noxbox clean() {
        id = null;
        ownerMessages.clear();
        partyMessages.clear();
        party = null;
        estimationTime = null;
        cancellationReasonMessage = null;
        commentForDemand = null;
        commentForSupply = null;
        timeCreated = null;
        timeRemoved = null;
        timeRequested = null;
        timeCompleted = null;
        timeAccepted = null;
        timeCanceledByOwner = null;
        timeCanceledByParty = null;
        timeOwnerVerified = null;
        timePartyVerified = null;
        timeOwnerDisliked = null;
        timePartyDisliked = null;
        timeToMeet = null;
        timeTimeout = null;

        return this;
    }

    public String getId() {
        return id;
    }

    public Noxbox setId(String id) {
        this.id = id;
        return this;
    }

    public Position getPosition() {
        return position;
    }

    public Noxbox setPosition(Position position) {
        this.position = position;
        return this;
    }

    public Long getTimeCompleted() {
        return timeCompleted;
    }

    public Noxbox setTimeCompleted(Long timeCompleted) {
        this.timeCompleted = timeCompleted;
        return this;
    }

    public String getPrice() {
        if (price == null) {
            price = "5";
        }
        return price;
    }

    public Noxbox setPrice(String price) {
        this.price = price;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Noxbox noxbox = (Noxbox) o;
        return id != null ? id.equals(noxbox.id) : noxbox.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(Noxbox that) {
        return that.getTimeCompleted().compareTo(this.getTimeCompleted());
    }

    public Long getTimeRequested() {
        return timeRequested;
    }

    public Noxbox setTimeRequested(Long timeRequested) {
        this.timeRequested = timeRequested;
        return this;
    }

    public Long getTimeCanceledByOwner() {
        return timeCanceledByOwner;
    }

    public Noxbox setTimeCanceledByOwner(Long timeCanceledByOwner) {
        this.timeCanceledByOwner = timeCanceledByOwner;
        return this;
    }

    public Long getTimeCanceledByParty() {
        return timeCanceledByParty;
    }

    public Noxbox setTimeCanceledByParty(Long timeCanceledByParty) {
        this.timeCanceledByParty = timeCanceledByParty;
        return this;
    }

    public Long getTimeAccepted() {
        return timeAccepted;
    }

    public Noxbox setTimeAccepted(Long timeAccepted) {
        this.timeAccepted = timeAccepted;
        return this;
    }

    public NoxboxType getType() {
        if (type == null) {
            type = NoxboxType.values()[0];
        }
        return type;
    }

    public Noxbox setType(NoxboxType type) {
        this.type = type;
        return this;
    }

    public Profile getOwner() {
        if (owner == null) {
            owner = new Profile();
        }
        return owner;
    }

    public Noxbox setOwner(Profile owner) {
        this.owner = owner;
        return this;
    }

    public Profile getParty() {
        return party;
    }

    public Noxbox setParty(Profile party) {
        this.party = party;
        return this;
    }

    public Profile getNotMe(String myId) {
        if (myId.equals(getOwner().getId())) return getParty();
        else return getOwner();
    }

    public Profile getMe(String myId) {
        if (owner.getId().equals(myId)) return owner;
        else return party;
    }

    public String getEstimationTime() {
        return estimationTime;
    }

    public Noxbox setEstimationTime(String estimationTime) {
        this.estimationTime = estimationTime;
        return this;
    }

    public MarketRole getRole() {
        if (role == null) {
            role = MarketRole.demand;
        }
        return role;
    }

    public Noxbox setRole(MarketRole role) {
        this.role = role;
        return this;
    }

    public Profile getPerformer() {
        if (role == MarketRole.supply) return owner;
        else return party;
    }

    public Profile getPayer() {
        if (role == MarketRole.demand) return owner;
        else return party;
    }

    public WorkSchedule getWorkSchedule() {
        if (workSchedule == null) {
            workSchedule = new WorkSchedule();
        }
        return workSchedule;
    }

    public Noxbox setWorkSchedule(WorkSchedule workSchedule) {
        this.workSchedule = workSchedule;
        return this;
    }

    public Long getTimeCreated() {
        return timeCreated;
    }

    public Noxbox setTimeCreated(Long timeCreated) {
        this.timeCreated = timeCreated;
        return this;
    }

    public Long getTimeOwnerVerified() {
        return timeOwnerVerified;
    }

    public Noxbox setTimeOwnerVerified(Long timeOwnerVerified) {
        this.timeOwnerVerified = timeOwnerVerified;
        return this;
    }

    public Long getTimePartyVerified() {
        return timePartyVerified;
    }

    public Noxbox setTimePartyVerified(Long timePartyVerified) {
        this.timePartyVerified = timePartyVerified;
        return this;
    }

    public Long getTimeStartPerforming() {
        if (getTimeOwnerVerified() == null || getTimePartyVerified() == null) return null;

        return Math.max(getTimeOwnerVerified(), getTimePartyVerified());
    }

    public String getCancellationReasonMessage() {
        return cancellationReasonMessage;
    }

    public Noxbox setCancellationReasonMessage(String cancellationReasonMessage) {
        this.cancellationReasonMessage = cancellationReasonMessage;
        return this;
    }

    public String getCommentForDemand() {
        return commentForDemand;
    }

    public Noxbox setCommentForDemand(String commentForDemand) {
        this.commentForDemand = commentForDemand;
        return this;
    }

    public String getCommentForSupply() {
        return commentForSupply;
    }

    public Noxbox setCommentForSupply(String commentForSupply) {
        this.commentForSupply = commentForSupply;
        return this;
    }

    public Long getTimeOwnerDisliked() {
        return timeOwnerDisliked;
    }

    public Noxbox setTimeOwnerDisliked(Long timeOwnerDisliked) {
        this.timeOwnerDisliked = timeOwnerDisliked;
        return this;
    }

    public Long getTimePartyDisliked() {
        return timePartyDisliked;
    }

    public Noxbox setTimePartyDisliked(Long timePartyDisliked) {
        this.timePartyDisliked = timePartyDisliked;
        return this;
    }

    public Long getTimeTimeout() {
        return timeTimeout;
    }

    public Noxbox setTimeTimeout(Long timeTimeout) {
        this.timeTimeout = timeTimeout;
        return this;
    }

    public Long getTimeToMeet() {
        return timeToMeet;
    }

    public Noxbox setTimeToMeet(Long timeToMeet) {
        this.timeToMeet = timeToMeet;
        return this;
    }

    public Long getTimeRemoved() {
        return timeRemoved;
    }

    public Noxbox setTimeRemoved(Long timeRemoved) {
        this.timeRemoved = timeRemoved;
        return this;
    }

    public String getGeoId() {
        return geoId;
    }

    public Noxbox setGeoId(String geoId) {
        this.geoId = geoId;
        return this;
    }

    public List<Message> getChat(String profileId) {
        List<Message> chat = new ArrayList<>();
        Collection<Message> myMessages = getPartyMessages().values();
        if(profileId.equals(getOwner().getId())) {
            myMessages = getOwnerMessages().values();
        }
        for(Message message : myMessages) {
            message.setMyMessage(true);
        }
        chat.addAll(getPartyMessages().values());
        chat.addAll(getOwnerMessages().values());

        Collections.sort(chat, new Comparator<Message>() {
            @Override
            public int compare(Message first, Message second) {
                return Long.compare(first.getTime(), second.getTime());
            }
        });

        return chat;
    }

    public Map<String, Message> getOwnerMessages() {
        if(ownerMessages == null) {
            ownerMessages = new HashMap<>();
        }
        return ownerMessages;
    }

    public Noxbox setOwnerMessages(Map<String, Message> ownerMessages) {
        this.ownerMessages = ownerMessages;
        return this;
    }

    public Map<String, Message> getPartyMessages() {
        if(partyMessages == null) {
            partyMessages = new HashMap<>();
        }
        return partyMessages;
    }

    public Noxbox setPartyMessages(Map<String, Message> partyMessages) {
        this.partyMessages = partyMessages;
        return this;
    }
}
