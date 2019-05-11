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

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static live.noxbox.Constants.DEFAULT_PRICE;
import static live.noxbox.model.MarketRole.demand;
import static live.noxbox.model.MarketRole.supply;
import static live.noxbox.model.TravelMode.none;

public class Noxbox implements Comparable<Noxbox> {

    private String id;
    private String geoId;
    @Virtual
    private Drawable confirmationPhoto;
    @Virtual
    private Boolean wasNotificationVerification;

    private Profile owner;
    private Profile party;
    private String performerId;
    private String payerId;

    private Chat chat;

    private Position position;
    private String price;
    private NoxboxType type;
    private MarketRole role;
    private WorkSchedule workSchedule;
    private String contractComment;

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
    private long timeOwnerRejected;
    private long timePartyRejected;
    private long timeOwnerDisliked;
    private long timeOwnerLiked;
    private long timePartyDisliked;

    private long timePartyLiked;
    private long timeTimeout;
    private long timeRatingUpdated;
    private String cancellationReasonMessage;
    private String ownerComment;
    private String partyComment;
    private boolean finished;

    public Noxbox copy(Noxbox from) {
        id = from.id;
        geoId = from.geoId;
        owner = new Profile().copy(from.owner);
        party = new Profile().copy(from.party);
        performerId = from.performerId;
        payerId = from.payerId;
        chat = from.chat;
        position = from.position;
        price = from.price;
        type = from.type;
        role = from.role;
        //workSchedule = from.getWorkSchedule();
        contractComment = from.contractComment;
        timeCreated = from.timeCreated;
        timeRemoved = from.timeRemoved;
        timeRequested = from.timeRequested;
        timeCompleted = from.timeCompleted;
        timeAccepted = from.timeAccepted;
        timeCanceledByOwner = from.timeCanceledByOwner;
        timeCanceledByParty = from.timeCanceledByParty;
        timeOwnerVerified = from.timeOwnerVerified;
        timePartyVerified = from.timePartyVerified;
        timeOwnerRejected = from.timeOwnerRejected;
        timePartyRejected = from.timePartyRejected;
        timeOwnerDisliked = from.timeOwnerDisliked;
        timeOwnerLiked = from.timeOwnerLiked;
        timePartyDisliked = from.timePartyDisliked;
        timePartyLiked = from.timePartyLiked;
        timeTimeout = from.timeTimeout;
        timeRatingUpdated = from.timeRatingUpdated;
        cancellationReasonMessage = from.cancellationReasonMessage;
        ownerComment = from.ownerComment;
        partyComment = from.partyComment;
        finished = from.finished;
        return this;
    }

    public Noxbox clean() {
        id = "";
        geoId = null;
        getChat().getOwnerMessages().clear();
        getChat().getPartyMessages().clear();
        getChat().setOwnerReadTime(0L);
        getChat().setPartyReadTime(0L);
        // party = new Profile().setId("0");
        confirmationPhoto = null;
        wasNotificationVerification = false;
        party = null;
        performerId = "";
        payerId = "";
        contractComment = null;
        cancellationReasonMessage = null;
        ownerComment = null;
        partyComment = null;
        timeCreated = 0L;
        timeRemoved = 0L;
        timeRequested = 0L;
        timeCompleted = 0L;
        timeAccepted = 0L;
        timeCanceledByOwner = 0L;
        timeCanceledByParty = 0L;
        timeOwnerVerified = 0L;
        timePartyVerified = 0L;
        timeOwnerRejected = 0L;
        timePartyRejected = 0L;
        timeOwnerDisliked = 0L;
        timeOwnerLiked = 0L;
        timePartyDisliked = 0L;
        timePartyLiked = 0L;
        timeTimeout = 0L;
        timeRatingUpdated = 0L;

        return this;
    }

    public Noxbox create(Position position, Profile owner) {
        clean();
        this.position = position;
        this.owner = owner;
        return this;
    }

    public Boolean getWasNotificationVerification() {
        if (wasNotificationVerification == null) {
            wasNotificationVerification = new Boolean(false);
        }
        return wasNotificationVerification;
    }

    public Noxbox setWasNotificationVerification(Boolean wasNotificationVerification) {
        this.wasNotificationVerification = wasNotificationVerification;
        return this;
    }

    public int getIcon() {
        if (getRole() == demand) {
            return getType().getImageDemand();
        }

        return getType().getImageSupply();
    }

    public Profile getProfileWhoComes() {
        if (party == null) return getOwner();
        if (getOwner().getTravelMode() == none) {
            return party;
        } else {
            if (role == supply) {
                return getOwner();
            }
        }
        if (party.getTravelMode() == none) {
            return getOwner();
        }
        return party;
    }

    public Profile getProfileWhoWait() {
        return getProfileWhoComes().equals(owner) ? party : owner;
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

    public long getTimeOwnerRejected() {
        return timeOwnerRejected;
    }

    public Noxbox setTimeOwnerRejected(long timeOwnerRejected) {
        this.timeOwnerRejected = timeOwnerRejected;
        return this;
    }

    public long getTimePartyRejected() {
        return timePartyRejected;
    }

    public Noxbox setTimePartyRejected(long timePartyRejected) {
        this.timePartyRejected = timePartyRejected;
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
            return 0L;

        return Math.max(getTimeOwnerVerified(), getTimePartyVerified());
    }

    public String getCancellationReasonMessage() {
        return cancellationReasonMessage;
    }

    public Noxbox setCancellationReasonMessage(String cancellationReasonMessage) {
        this.cancellationReasonMessage = cancellationReasonMessage;
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

    public String getPartyComment() {
        if (partyComment == null) {
            partyComment = "";
        }
        return partyComment;
    }

    public Noxbox setPartyComment(String partyComment) {
        this.partyComment = partyComment;
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
        if (performerId == null) {
            performerId = "";
        }
        return performerId;
    }

    public Noxbox setPerformerId(String performerId) {
        this.performerId = performerId;
        return this;
    }

    public String getPayerId() {
        if (payerId == null) {
            payerId = "";
        }
        return payerId;
    }

    public Noxbox setPayerId(String payerId) {
        this.payerId = payerId;
        return this;
    }

    public String getContractComment() {
        if (contractComment == null) {
            contractComment = "";
        }
        return contractComment;
    }

    public Noxbox setContractComment(String contractComment) {
        this.contractComment = contractComment;
        return this;
    }

    public static boolean isNullOrZero(Long time) {
        return time == null || time == 0;
    }

    public static boolean isNullOrZero(Integer number) {
        return number == null || number == 0;
    }

    public Noxbox setFinished(boolean finished) {
        this.finished = finished;
        return this;
    }

    public boolean getFinished() {
        return finished;
    }

    public MarketRole getMyRole(String myId) {
        return myId.equals(getOwner().getId()) && role == demand ? demand : supply;
    }

    public Drawable getConfirmationPhoto() {
        return confirmationPhoto;
    }

    public Noxbox setConfirmationPhoto(Drawable confirmationPhoto) {
        this.confirmationPhoto = confirmationPhoto;
        return this;
    }

    @Override
    public String toString() {
        String string =
                "id=" + getId();
        return string;
    }

    public long getTimeRatingUpdated() {
        return timeRatingUpdated;
    }

    public Noxbox setTimeRatingUpdated(long timeRatingUpdated) {
        this.timeRatingUpdated = timeRatingUpdated;
        return this;
    }

    public long getTimeOwnerLiked() {
        return timeOwnerLiked;
    }

    public Noxbox setTimeOwnerLiked(long timeOwnerLiked) {
        this.timeOwnerLiked = timeOwnerLiked;
        return this;
    }

    public long getTimePartyLiked() {
        return timePartyLiked;
    }

    public Noxbox setTimePartyLiked(long timePartyLiked) {
        this.timePartyLiked = timePartyLiked;
        return this;
    }

}
