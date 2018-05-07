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

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.payer.massage.R;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.addMessage;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.removeMessage;

public abstract class ChatActivity extends SwitchActivity {

    private ImageView chatIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChatIcon().setVisibility(View.VISIBLE);
    }

    @Override
    protected void processNoxbox(Noxbox noxbox) {
        super.processNoxbox(noxbox);

        getChatIcon().setVisibility(View.VISIBLE);
        if(hasNewMessages(noxbox)) {
            //update button with
        }
        //open full screen chat
    }

    protected ImageView getChatIcon() {
        if(chatIcon == null) {
            int size = 196;
            chatIcon = new ImageView(this);
            chatIcon.setImageResource(R.drawable.chat);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size,
                    Gravity.END | Gravity.CENTER_VERTICAL);
            params.setMarginEnd(40);
            chatIcon.setLayoutParams(params);
            chatIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup("Bu!");
                }
            });

            FrameLayout layout = findViewById(R.id.frame_layout);
            layout.addView(chatIcon);
        }

        return chatIcon;
    }

    private boolean hasNewMessages(Noxbox noxbox) {
        if(noxbox.getChat() == null || noxbox.getChat().isEmpty()) {
            return false;
        }
        for(Message message : noxbox.getChat().values()) {
            if(!message.getSender().getId().equals(getProfile().getId()) && !message.getWasRead()) {
                return true;
            }
        }
        return false;
    }

    protected void processStory(Message story) {
        popup("Message received: " + story.getStory());
        addMessage(story);
        removeMessage(story.getId());
    }

    @Override
    protected void prepareForIteration() {
        super.prepareForIteration();
        // button disappear
    }
}
