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

import by.nicolay.lipnevich.noxbox.model.Event;
import by.nicolay.lipnevich.noxbox.tools.Firebase;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getRating;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.persistHistory;

public abstract class EventFunction extends GeoFunction {

    protected void processRequest(Event request) {}
    protected void processAccept(Event accept) {}
    protected void processPerformerCancel(Event performerCancel) {}
    protected void processPayerCancel(Event payerCancel) {}
    protected void processMove(Event move) {}
    protected void processSync(Event sync) {}
    protected void processQr(Event qr) {
        // TODO (nli) update ui for both users
    }
    protected void processStory(Event story) {}
    protected void processDislike(Event dislike) {
        getRating().getReceived().setLikes(getRating().getReceived().getLikes() - 1);
        getRating().getReceived().setDislikes(getRating().getReceived().getDislikes() + 1);

        processProfile(getProfile());
    }
    private void processBalanceUpdated(Event update) {
        Firebase.updateWallet(update.getWallet());
    }

    @Override
    public void processEvent(Event event) {
        switch (event.getType()) {
            case request: processRequest(event); break;
            case sync: processSync(event); break;
            case accept: processAccept(event); break;
            case performerCancel: processPerformerCancel(event); break;
            case payerCancel: processPayerCancel(event); break;
            case qr: processQr(event); break;
            case complete:
                persistHistory();
                prepareForIteration();
                break;
            case dislike: processDislike(event); break;
            case move: processMove(event); break;
            case story: processStory(event); break;
            case balance: processBalanceUpdated(event); break;
        }
    }


}
