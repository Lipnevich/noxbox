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
package live.noxbox.old;

public abstract class EventFunction extends GeoFunction {
/*

    protected void processRequest(Message request) {}
    protected void processAccept(Message accept) {}
    protected void processPerformerCancel(Message performerCancel) {}
    protected void processPayerCancel(Message payerCancel) {}
    protected void processMove(Message move) {}
    protected void processSync(Message sync) {}
    protected void processQr(Message qr) {
        // TODO (nli) update ui for both users
    }
    protected void processStory(Message story) {}
    protected void processDislike(Message dislike) {
        getRating().getReceived().setLikes(getRating().getReceived().getLikes() - 1);
        getRating().getReceived().setDislikes(getRating().getReceived().getDislikes() + 1);

        processProfile(getProfile());
    }
    private void processBalanceUpdated(Message update) {
        GeoRealtime.updateWallet(update.getWallet());
    }

    public void processEvent(Message event) {
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

*/

}
