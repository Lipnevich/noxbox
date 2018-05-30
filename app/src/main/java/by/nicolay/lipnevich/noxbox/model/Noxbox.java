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
package by.nicolay.lipnevich.noxbox.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Noxbox implements Comparable<Noxbox> {

    private String id;
    private Profile performer;
    private Profile payer;
    private Map<String, Event> chat = new HashMap<>();

    // Noxbox specific data
    private Long timeRequested;
    private Long timeCompleted;
    private Long timeAccepted;
    private Long timeCanceled;

    private Position position;
    private String price;
    private NoxboxType type;
    private String estimationTime;

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

    public Long getTimeCanceled() {
        return timeCanceled;
    }

    public Noxbox setTimeCanceled(Long timeCanceled) {
        this.timeCanceled = timeCanceled;
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

    public Profile getPerformer() {
        return performer;
    }

    public Noxbox setPerformer(Profile performer) {
        this.performer = performer;
        return this;
    }

    public Profile getPayer() {
        return payer;
    }

    public Noxbox setPayer(Profile payer) {
        this.payer = payer;
        return this;
    }

    @Exclude
    public Profile getParty(String myId) {
        if(performer.getId().equals(myId)) return payer;
        else return performer;
    }

    @Exclude
    public Profile getMe(String myId) {
        if(performer.getId().equals(myId)) return performer;
        else return payer;
    }

    public String getEstimationTime() {
        return estimationTime;
    }

    public Noxbox setEstimationTime(String estimationTime) {
        this.estimationTime = estimationTime;
        return this;
    }
}
