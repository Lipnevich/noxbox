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
package by.nicolay.lipnevich.noxbox;

import java.util.HashMap;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.tools.Firebase;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.createHistory;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getRating;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.removeMessage;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNoxboxInProgress;
import static java.lang.System.currentTimeMillis;

public abstract class NoxboxActivity extends GeoFireComponent {

    protected void processPing(Message ping) {}
    protected void processPong(Message pong) {}
    protected void processGnop(Message gnop) {}
    protected void processMove(Message move) {}
    private void processQr(Message qr) {
        // TODO (nli) update ui for both users
        removeMessage(qr.getId());
    }
    protected void processStory(Message story) {
        popup("Message received: " + story.getStory());
        // TODO (nli) sound

        removeMessage(story.getId());
    }
    protected void processDislike(Message rate) {
        getRating().getReceived().setLikes(getRating().getReceived().getLikes() - 1);
        getRating().getReceived().setDislikes(getRating().getReceived().getDislikes() + 1);

        processProfile(getProfile());
        removeMessage(rate.getId());
    }
    private void processBalanceUpdated(Message update) {
        Firebase.updateWallet(update.getWallet());
        removeMessage(update.getId());
    }

    @Override
    protected void processMessage(Message message) {
        switch (message.getType()) {
            case ping: processPing(message); break;
            case pong: processPong(message); break;
            case gnop: processGnop(message); break;
            case qr: processQr(message); break;
            case complete:
                if(tryGetNoxboxInProgress() != null) createHistory(tryGetNoxboxInProgress()
                        .setTimeCompleted(currentTimeMillis()));
                Firebase.like();
                prepareForIteration();
                removeMessage(message.getId());
                break;
            case dislike: processDislike(message); break;
            case move: processMove(message); break;
            case story: processStory(message); break;
            case balanceUpdated: processBalanceUpdated(message); break;
        }
    }

    protected Map<String, Profile> map(String id, Profile profile) {
        Map<String, Profile> map = new HashMap<>();
        map.put(id, profile);
        return map;
    }
}
