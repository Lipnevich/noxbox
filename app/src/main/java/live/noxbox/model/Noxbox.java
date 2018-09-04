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

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Noxbox implements Comparable<Noxbox> {

    private String id;
    private Profile owner;
    private Profile party;
    private Map<String, Event> chat = new HashMap<>();

    // Noxbox specific data
    private Long timeCreated;
    private Long timeRequested;
    private Long timeCompleted;
    private Long timeAccepted;
    private Long timeCanceledBySupplier;
    private Long timeCanceledByDemander;

    private Long timeSupplyVerified;
    private Long timeDemandVerified;

    private Long timeStartPerforming;

    private Long totalExecutionTimeInMinutes;

    private Position position;
    private String price;
    private NoxboxType type;
    private MarketRole role;
    private WorkSchedule workSchedule;
    private String estimationTime;

    private String cancellationReasonMessage;
    private String commentForDemand;
    private String commentForSupply;


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

    public Long getTimeCanceledBySupplier() {
        return timeCanceledBySupplier;
    }

    public Noxbox setTimeCanceledBySupplier(Long timeCanceledBySupplier) {
        this.timeCanceledBySupplier = timeCanceledBySupplier;
        return this;
    }

    public Long getTimeCanceledByDemander() {
        return timeCanceledByDemander;
    }

    public Noxbox setTimeCanceledByDemander(Long timeCanceledByDemander) {
        this.timeCanceledByDemander = timeCanceledByDemander;
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
        return type;
    }

    public Noxbox setType(NoxboxType type) {
        this.type = type;
        return this;
    }

    public Map<String, Event> getChat() {
        return chat;
    }

    public Noxbox setChat(Map<String, Event> chat) {
        this.chat = chat;
        return this;
    }

    public Profile getOwner() {
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

    @Exclude
    public Profile getNotMe(String myId) {
        if(owner.getId().equals(myId)) return party;
        else return owner;
    }

    @Exclude
    public Profile getMe(String myId) {
        if(owner.getId().equals(myId)) return owner;
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
        return role;
    }

    public Noxbox setRole(MarketRole role) {
        this.role = role;
        return this;
    }

    @Exclude
    public Profile getPerformer() {
        if(role == MarketRole.supply) return owner;
        else return party;
    }

    @Exclude
    public Profile getPayer() {
        if(role == MarketRole.demand) return owner;
        else return party;
    }

    public WorkSchedule getWorkSchedule() {
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

    public Long getTimeSupplyVerified() {
        return timeSupplyVerified;
    }

    public Noxbox setTimeSupplyVerified(Long timeSupplyVerified) {
        this.timeSupplyVerified = timeSupplyVerified;
        return this;
    }

    public Long getTimeDemandVerified() {
        return timeDemandVerified;
    }

    public Noxbox setTimeDemandVerified(Long timeDemandVerified) {
        this.timeDemandVerified = timeDemandVerified;
        return this;
    }

    public Long getTotalExecutionTimeInMinutes() {
        return totalExecutionTimeInMinutes;
    }

    public Noxbox setTotalExecutionTimeInMinutes(Long totalExecutionTimeInMinutes) {
        this.totalExecutionTimeInMinutes = totalExecutionTimeInMinutes;
        return this;
    }

    public Long getTimeStartPerforming() {
        return timeStartPerforming;
    }

    public Noxbox setTimeStartPerforming(Long timeStartPerforming) {
        this.timeStartPerforming = timeStartPerforming;
        return this;
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
}
