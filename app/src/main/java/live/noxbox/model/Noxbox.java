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
import java.util.List;

import static live.noxbox.Constants.DEFAULT_PRICE;
import static live.noxbox.model.MarketRole.supply;
import static live.noxbox.model.TravelMode.none;

public class Noxbox implements Comparable<Noxbox> {

    private String id;
    @Virtual
    private String geoId;

    private Profile owner;
    private Profile party;
    private String performerId;
    private String payerId;

    private Chat chat;

    private Position position;
    private String price;
    private String total;
    private NoxboxType type;
    private MarketRole role;
    private WorkSchedule workSchedule;
    private String ownerComment;


    // Noxbox specific data
    private long timeCreated;
    private long timeRemoved;
    private long timeRequested;
    private long timeCompleted;
    private long timeAccepted;
    private long timeCanceledByOwner;
    private long timeCanceledByParty;
    private long timeOwnerVerified;
    private long timePartyVerified;
    private long timeOwnerDisliked;
    private long timePartyDisliked;
    private long timeTimeout;
    private String cancellationReasonMessage;
    private String commentForDemand;
    private String commentForSupply;
    private boolean finished;


    public Noxbox clean() {
        id = "";
        getChat().getOwnerMessages().clear();
        getChat().getPartyMessages().clear();
        getChat().setOwnerReadTime(0L);
        getChat().setPartyReadTime(0L);
        // party = new Profile().setId("0");
        party = null;
        performerId = "";
        payerId = "";
        ownerComment = null;
        cancellationReasonMessage = null;
        commentForDemand = null;
        commentForSupply = null;
        timeCreated = 0L;
        timeRemoved = 0L;
        timeRequested = 0L;
        timeCompleted = 0L;
        timeAccepted = 0L;
        timeCanceledByOwner = 0L;
        timeCanceledByParty = 0L;
        timeOwnerVerified = 0L;
        timePartyVerified = 0L;
        timeOwnerDisliked = 0L;
        timePartyDisliked = 0L;
        timeTimeout = 0L;

        return this;
    }

    public Noxbox create(Position position, Profile owner) {
        clean();
        this.position = position;
        this.owner = owner;
        return this;
    }


    public Profile getProfileWhoWait() {
        if (getProfileWhoComes().equals(owner)) {
            return party;
        } else {
            return owner;
        }
    }

    public Profile getProfileWhoComes() {
        if (owner.getTravelMode() == none) {
            return party;
        } else {
            if (role == supply) {
                return owner;
            }
        }
        if (party.getTravelMode() == none) {
            return owner;
        }
        return party;
    }

    public String getId() {
        if (id == null) {
            id = "";
        }
        return id;
    }

    public Noxbox setId(String id) {
        this.id = id;
        return this;
    }

    public String getTotal() {
        return total;
    }

    public Noxbox setTotal(String total) {
        this.total = total;
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
            price = DEFAULT_PRICE;
        }
        return price.replaceAll(",", "\\.");
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
        if (isNullOrZero(getTimeOwnerVerified()) || isNullOrZero(getTimePartyVerified()))
            return null;

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

    public Chat getChat() {
        if (chat == null) {
            chat = new Chat();
        }
        return chat;
    }

    public Noxbox setChat(Chat chat) {
        this.chat = chat;
        return this;
    }

    public List<Message> getMessages(String profileId) {
        List<Message> chat = new ArrayList<>();
        Collection<Message> myMessages = getChat().getPartyMessages().values();
        if (profileId.equals(getOwner().getId())) {
            myMessages = getChat().getOwnerMessages().values();
        }
        for (Message message : myMessages) {
            message.setMyMessage(true);
        }
        chat.addAll(getChat().getPartyMessages().values());
        chat.addAll(getChat().getOwnerMessages().values());

        Collections.sort(chat, (first, second) -> Long.compare(first.getTime(), second.getTime()));

        return chat;
    }


    public String getPerformerId() {
        if(performerId == null){
            performerId = "";
        }
        return performerId;
    }

    public Noxbox setPerformerId(String performerId) {
        this.performerId = performerId;
        return this;
    }

    public String getPayerId() {
        if(payerId == null){
            payerId = "";
        }
        return payerId;
    }

    public Noxbox setPayerId(String payerId) {
        this.payerId = payerId;
        return this;
    }

    public String getOwnerComment() {
        if (ownerComment == null) {
            ownerComment = "";
        }
        return ownerComment;
    }

    public Noxbox setOwnerComment(String ownerComment) {
        this.ownerComment = ownerComment;
        return this;
    }

    public static boolean isNullOrZero(Long time) {
        return time == null || time == 0;
    }

    public Noxbox setFinished(boolean finished) {
        this.finished = finished;
        return this;
    }

    public boolean getFinished() {
        return finished;
    }
}
