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

public abstract class ChatFunction {

   /* private ImageView chatIcon;

    @Override
    public void processNoxbox(Noxbox noxbox) {
        super.processNoxbox(noxbox);

        getChatIcon().setVisibility(View.VISIBLE);
        if(hasNewMessages(noxbox)) {
            drawNewMessageSign();
        }
    }

    private void drawNewMessageSign() {
        getChatIcon().setImageResource(R.drawable.chat_unread);
    }

    protected ImageView getChatIcon() {
        if(chatIcon == null) {
            chatIcon = findViewById(R.id.chat);
            chatIcon.setImageResource(R.drawable.chat);
        }

        chatIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), ChatActivity.class),
                        ChatActivity.CODE);
            }
        });

        return chatIcon;
    }

    private boolean hasNewMessages(Noxbox noxbox) {
        if(noxbox == null || noxbox.getChat() == null || noxbox.getChat().isEmpty()) {
            return false;
        }
        for(Message event : noxbox.getChat().values()) {
            if(!event.getSender().getIndex().equals(getProfile().getIndex()) && !event.getWasRead()) {
                return true;
            }
        }
        return false;
    }

    protected void processStory(Message story) {
        if(getCurrentNoxbox() != null) {
            addMessageToChat(story.setWasRead(false));
            drawNewMessageSign();
        }
    }

    @Override
    public void prepareForIteration() {
        super.prepareForIteration();
        getChatIcon().setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ChatActivity.CODE && GeoRealtime.getProfile() != null) {
            if(hasNewMessages(getCurrentNoxbox())) {
                drawNewMessageSign();
            } else {
                getChatIcon().setImageResource(R.drawable.chat);
            }
//            listenEvents();
        }
    }*/
}
