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

import java.util.HashMap;
import java.util.Map;

public class Noxbox implements Comparable<Noxbox> {

    private String id;
    private Map<String, Profile> performers = new HashMap<>();
    private Map<String, Profile> payers = new HashMap<>();
    private Map<String, Message> chat = new HashMap<>();

    // Noxbox specific data
    private Long timeRequested;
    private Long timeCompleted;
    private Long timeAccepted;
    private Long timeCanceled;
    private Position position;
    private Boolean rate;
    private String price;
    private String priceWithoutFee;
    private NoxboxType type;
    private String estimationTime;

    public String getId() {
        return id;
    }

    public Noxbox setId(String id) {
        this.id = id;
        return this;
    }

    public Map<String, Profile> getPerformers() {
        return performers;
    }

    public Noxbox setPerformers(Map<String, Profile> performers) {
        this.performers = performers;
        return this;
    }

    public Map<String, Profile> getPayers() {
        return payers;
    }

    public Noxbox setPayers(Map<String, Profile> payers) {
        this.payers = payers;
        return this;
    }

    public String getEstimationTime() {
        return estimationTime;
    }

    public Noxbox setEstimationTime(String estimationTime) {
        this.estimationTime = estimationTime;
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

    public Boolean getRate() {
        return rate;
    }

    public Noxbox setRate(Boolean rate) {
        this.rate = rate;
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

    public String getPriceWithoutFee() {
        return priceWithoutFee;
    }

    public Noxbox setPriceWithoutFee(String priceWithoutFee) {
        this.priceWithoutFee = priceWithoutFee;
        return this;
    }

    public Map<String, Message> getChat() {
        return chat;
    }

    public Noxbox setChat(Map<String, Message> chat) {
        this.chat = chat;
        return this;
    }
}
