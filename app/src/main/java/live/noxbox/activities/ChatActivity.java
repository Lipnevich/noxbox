package live.noxbox.activities;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.model.Message;
import live.noxbox.model.Profile;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static java.util.Collections.sort;

public class ChatActivity extends BaseActivity {

    public static final int CODE = 1001;

    private LinearLayout root;
    private RecyclerView chatList;
    private EditText text;

    private ChatAdapter chatAdapter;

    private List<Message> messages = new ArrayList<>();
    private long timeWasRead = -1L;

    private DisplayMetrics screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_chat);

        root = findViewById(R.id.root);
        text = findViewById(R.id.type_message);
        chatList = findViewById(R.id.chat_list);
        chatList.setHasFixedSize(true);
        chatList.setLayoutManager(new LinearLayoutManager(this));

        screen = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(screen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.back).setOnClickListener(v -> Router.finishActivity(ChatActivity.this));

        AppCache.listenProfile(ChatActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(final Profile profile) {
                draw(profile);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(ChatActivity.class.getName());
    }

    private void draw(final Profile profile) {
        //Drawable drawable = getDrawable(profile.getContract().getType().getIllustration());
       // root.setBackground(drawable);

        if (profile.getCurrent().getOwner().equals(profile)) {
            if (timeWasRead == profile.getCurrent().getChat().getPartyReadTime()
                    && messages.size() == profile.getCurrent().getMessages(profile.getId()).size()) {
                return;
            }
            timeWasRead = profile.getCurrent().getChat().getPartyReadTime();
        } else {
            if (timeWasRead == profile.getCurrent().getChat().getOwnerReadTime()
                    && messages.size() == profile.getCurrent().getMessages(profile.getId()).size()) {
                return;
            }
            timeWasRead = profile.getCurrent().getChat().getOwnerReadTime();
        }

        TextView interlocutorName = findViewById(R.id.chat_opponent_name);
        interlocutorName.setText(profile.getCurrent().getNotMe(profile.getId()).getName());

        initMessages(profile);

        chatAdapter = new ChatAdapter(screen, messages, this.getApplicationContext(), profile);
        chatList.setAdapter(chatAdapter);
        chatList.smoothScrollToPosition(View.FOCUS_DOWN);

        text.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                send(profile);
                return true;
            }
            return false;
        });

        findViewById(R.id.send_message).setOnClickListener(v -> send(profile));
    }

    private void send(Profile profile) {
        String trimmedText = text.getText().toString().trim();
        if (TextUtils.isEmpty(trimmedText)) return;
        sound();

        long time = System.currentTimeMillis();

        Message message = new Message().setMessage(trimmedText)
                .setId("" + time).setTime(time);
        if (profile.equals(profile.getCurrent().getOwner())) {
            profile.getCurrent().getChat().getOwnerMessages().put(message.getId(), message);
        } else {
            profile.getCurrent().getChat().getPartyMessages().put(message.getId(), message);
        }
        text.setText("");
        add(message);
        AppCache.updateNoxbox();
        initMessages(profile);
    }

    private void add(Message message) {
        messages.add(message);
        sort(messages);
        chatAdapter.notifyItemInserted(messages.indexOf(message));
        chatList.scrollToPosition(messages.size() - 1);
    }



    private List<Message> initMessages(Profile profile) {
        //messages.clear();

        if (profile.getCurrent() != null) {

            List<Message> remoteMessages = profile.getCurrent().getMessages(profile.getId());
            List<Message> newMessages = new ArrayList<>();
            for (Message fromRemote : remoteMessages) {
                boolean wasFind = false;
                for (Message fromLocale : messages) {
                    if (fromLocale.getTime().equals(fromRemote.getTime())) {
                        wasFind = true;
                    }
                }

                if (!wasFind) {
                    newMessages.add(fromRemote);
                }
            }
            for (Message newMessage : newMessages) {
                messages.add(newMessage);
            }

            //messages.addAll(remoteMessages);
            sort(messages);
        }


        return messages;
    }
    private void sound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(ChatActivity.this, R.raw.message);
        mediaPlayer.start();
    }
}
