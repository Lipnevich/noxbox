package by.nicolay.lipnevich.noxbox.pages;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.MessageType;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.UserType;
import by.nicolay.lipnevich.noxbox.payer.massage.BuildConfig;
import by.nicolay.lipnevich.noxbox.payer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.Task;
import by.nicolay.lipnevich.noxbox.tools.TimeLog;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.addMessageToChat;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getUserType;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.listenTypeMessages;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.removeMessage;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.sendMessageForNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNoxboxInProgress;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.updateCurrentNoxbox;
import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;

public class ChatPage extends AppCompatActivity {

    private List<Message> messages = new ArrayList<>();
    private RecyclerView chatList;
    private TextView text;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long millis = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        setTitle(getInterlocutor().getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TimeLog.log(millis, TimeLog.Event.onCreateChat, getApplicationContext());

        chatList = findViewById(R.id.chat_list);
        chatList.setHasFixedSize(true);
        chatList.setLayoutManager(new LinearLayoutManager(this));

        final DisplayMetrics screen = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(screen);

        Glide.with(getApplicationContext())
            .asBitmap()
            .load(R.drawable.view)
            .apply(diskCacheStrategyOf(BuildConfig.DEBUG ? DiskCacheStrategy.NONE : DiskCacheStrategy.RESOURCE))
            .into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap picture, Transition<? super Bitmap> transition) {
                    // here I was thinking about some cool lib for processing background images in the Android
                    // with cropping selecting area without image collisions on all possible screens,
                    // active on screen rotate, with resizing and even blur!

                    double koef = ((double)screen.heightPixels) / ((double)screen.widthPixels);
                    int width = width(picture, koef);
                    int height = height(picture, width, koef);
                    Bitmap background;
                    if(koef > 1) {
                        // vertical layout
                        background = blur(0, resize(screen, right(picture, width, height)));
                    } else {
                        // horizontal layout
                        background = blur(0, resize(screen, left(picture, width, height)));
                    }
                    getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), background));
        }});

        initMessages();
        chatAdapter = new ChatAdapter(screen, messages, getProfile().getId());
        chatList.setAdapter(chatAdapter);
        chatList.scrollToPosition(messages.size() - 1);

        text = findViewById(R.id.type_message);
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    send();
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });

        listenTypeMessages(MessageType.story, new Task<Message>() {
            @Override
            public void execute(Message story) {
                if(story.getType().equals(MessageType.story)) {
                    add(story.setWasRead(true));
                    removeMessage(story);
                }
            }
        });
    }

    private int height(Bitmap picture, int width, double koef) {
        int height = (int)(width * koef);
        if(picture.getHeight() < height) {
            height = picture.getHeight();
        }
        return height;
    }

    private int width(Bitmap picture, double koef) {
        int width = (int)(picture.getHeight() / koef);
        if(picture.getWidth() < width) {
            width = picture.getWidth();
        }
        return width;
    }

    private Bitmap resize(DisplayMetrics screen, Bitmap picture) {
        return createScaledBitmap(picture, screen.widthPixels, screen.heightPixels, false);
    }

    private Bitmap blur(int blurRadius, Bitmap picture) {
        if(blurRadius == 0) return picture;

        RenderScript rs = RenderScript.create(getApplicationContext());

        Allocation in = Allocation.createFromBitmap(rs, picture,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);

        Allocation out = Allocation.createTyped(rs, in.getType());
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, out.getElement());
        blur.setRadius(blurRadius);
        blur.setInput(in);
        blur.forEach(out);

        out.copyTo(picture);
        rs.destroy();

        return picture;
    }

    private Bitmap right(Bitmap picture, int width, int height) {
        return createBitmap(picture, picture.getWidth() - width, 0, width, height);
    }

    private Bitmap left(Bitmap picture, int width, int height) {
        return createBitmap(picture, 0, picture.getHeight() - height, width, height);
    }

    private void send() {
        if(TextUtils.isEmpty(text.getText().toString().trim())) return;

        Message message = sendMessageForNoxbox(new Message()
                .setType(MessageType.story)
                .setStory(text.getText().toString()));

        add(message.setWasRead(true));
        text.setText("");
    }

    private void add(Message message) {
        addMessageToChat(message.setWasRead(true));
        messages.add(message);
        play();
        sort(messages);

        chatAdapter.notifyItemRangeChanged(messages.size() > 1 ? messages.size() - 1 : 0,
                messages.size() - 1);
        chatList.scrollToPosition(messages.size() - 1);
    }

    private void play() {
        MediaPlayer mediaPlayer = MediaPlayer.create(ChatPage.this, R.raw.message);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                mp.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
            }
        });

    }

    private List<Message> initMessages() {
        messages.clear();

        if(tryGetNoxboxInProgress() != null) {
            messages.addAll(tryGetNoxboxInProgress().getChat().values());
            sort(messages);
        }

        return messages;
    }

    private void sort(List<Message> messages) {
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                ;                    return Long.valueOf(o1.getTime())
                        .compareTo(Long.valueOf(o2.getTime()));
            }
        });
    }

    private Profile getInterlocutor() {
        if(UserType.payer.equals(getUserType())) {
            return tryGetNoxboxInProgress().getPerformers().values().iterator().next();
        } else if (UserType.performer.equals(getUserType())) {
            return tryGetNoxboxInProgress().getPayers().values().iterator().next();
        } else {
            throw new IllegalArgumentException("Unknown role");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                updateCurrentNoxbox(tryGetNoxboxInProgress());
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
