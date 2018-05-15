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

public class Message implements Comparable<Message> {

    private String id, story, estimationTime;
    private Boolean wasRead;
    private Wallet wallet;
    private Profile sender, performer, payer;
    private MessageType type;
    private Position position;
    private Boolean like;
    private Long time;
    private Noxbox noxbox;
    private Push push;

    public String getId() {
        return id;
    }

    public Message setId(String id) {
        this.id = id;
        return this;
    }

    public MessageType getType() {
        return type;
    }

    public Message setType(MessageType type) {
        this.type = type;
        return this;
    }

    public Position getPosition() {
        return position;
    }

    public Message setPosition(Position position) {
        this.position = position;
        return this;
    }

    public String getStory() {
        return story;
    }

    public Message setStory(String story) {
        this.story = story;
        return this;
    }

    public Long getTime() {
        return time;
    }

    public Message setTime(Long time) {
        this.time = time;
        return this;
    }

    public Profile getSender() {
        return sender;
    }

    public Message setSender(Profile sender) {
        this.sender = sender;
        return this;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Message setWallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    public Noxbox getNoxbox() {
        return noxbox;
    }

    public Message setNoxbox(Noxbox noxbox) {
        this.noxbox = noxbox;
        return this;
    }

    public Boolean getLike() {
        return like;
    }

    public Message setLike(Boolean like) {
        this.like = like;
        return this;
    }

    public Profile getPerformer() {
        return performer;
    }

    public void setPerformer(Profile performer) {
        this.performer = performer;
    }

    public Profile getPayer() {
        return payer;
    }

    public void setPayer(Profile payer) {
        this.payer = payer;
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

    public Message setWasRead(Boolean wasRead) {
        this.wasRead = wasRead;
        return this;
    }

    public Push getPush() {
        return push;
    }

    public Message setPush(Push push) {
        this.push = push;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id != null ? id.equals(message.id) : message.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(Message that) {
        return this.getTime().compareTo(that.getTime());
    }
}
