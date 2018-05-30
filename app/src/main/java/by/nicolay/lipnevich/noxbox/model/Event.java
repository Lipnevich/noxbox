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

public class Event implements Comparable<Event> {

    private String id, message, estimationTime;
    private Boolean wasRead;
    private Wallet wallet;
    private Profile sender;
    private EventType type;
    private Position position;
    private Long time;
    private Noxbox noxbox;
    private Push push;

    public String getId() {
        return id;
    }

    public Event setId(String id) {
        this.id = id;
        return this;
    }

    public EventType getType() {
        return type;
    }

    public Event setType(EventType type) {
        this.type = type;
        return this;
    }

    public Position getPosition() {
        return position;
    }

    public Event setPosition(Position position) {
        this.position = position;
        return this;
    }

    public Long getTime() {
        return time;
    }

    public Event setTime(Long time) {
        this.time = time;
        return this;
    }

    public Profile getSender() {
        return sender;
    }

    public Event setSender(Profile sender) {
        this.sender = sender;
        return this;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Event setWallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    public Noxbox getNoxbox() {
        return noxbox;
    }

    public Event setNoxbox(Noxbox noxbox) {
        this.noxbox = noxbox;
        return this;
    }

    public String getEstimationTime() {
        return estimationTime;
    }

    public void setEstimationTime(String estimationTime) {
        this.estimationTime = estimationTime;
    }

    public Boolean getWasRead() {
        return wasRead;
    }

    public Event setWasRead(Boolean wasRead) {
        this.wasRead = wasRead;
        return this;
    }

    public Push getPush() {
        return push;
    }

    public Event setPush(Push push) {
        this.push = push;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Event setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return id != null ? id.equals(event.id) : event.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(Event that) {
        return this.getTime().compareTo(that.getTime());
    }
}
