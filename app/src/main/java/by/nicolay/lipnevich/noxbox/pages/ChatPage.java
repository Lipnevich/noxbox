package by.nicolay.lipnevich.noxbox.pages;


import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.UserType;
import by.nicolay.lipnevich.noxbox.payer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.TimeLog;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getUserType;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNoxboxInProgress;

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

//        Glide.with(getApplicationContext())
//            .asBitmap()
//            .load(R.drawable.chat_theme)
//            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
//            .into(new SimpleTarget<Bitmap>() {
//                @Override
//                public void onResourceReady(Bitmap picture, Transition<? super Bitmap> transition) {
//                    double koef = screen.heightPixels / screen.widthPixels;
//                    if(koef > 1) {
//                        // it is PHONE since height is bigger than width
//                        int width = (int) (picture.getHeight() / koef);
//
//                        Bitmap phone = Bitmap.createBitmap(picture, picture
//                                .getWidth() - width, 0, width, picture.getHeight());
//                        getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), phone));
//                    } else {
//                        // it is TABLET or square shaped device :)
//                        int height = (int) (picture.getWidth() * koef);
//                        Bitmap tabletBitmap = Bitmap.createBitmap(picture, picture
//                                .getWidth() - width, 0, width, picture.getHeight());
//                        Drawable drawable = new BitmapDrawable(getResources(), picture);
//                        getWindow().setBackgroundDrawable(drawable);
//                    }
//
//
//
//                    int height = 0;
//                    double k = 0;
//                    if(picture.getHeight() > screen.heightPixels) {
//                        height = screen.heightPixels;
//                        k = height / picture.getHeight();
//                    }
//                    picture.getWidth();
//
//
////                    phoneSize.widthPixels;
//
//
////                    Bitmap croppedBitmap = Bitmap.createBitmap(realImageSize, x, y, width, height);
//                    Drawable drawable = new BitmapDrawable(getResources(), picture);
//                    getWindow().setBackgroundDrawable(drawable);
//                }
//        });

        initMessages();
        initMessages();
//       final ChatAdapter chatAdapter = new ChatAdapter(tryGetNoxboxInProgress().getChat().values());
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
    }

    private void send() {
        if(TextUtils.isEmpty(text.getText().toString().trim())) return;

        messages.add(new Message().setStory(text.getText().toString()).setSender(getProfile()).setTime(System.currentTimeMillis()));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatList.scrollToPosition(messages.size() - 1);
        text.setText("");
    }

    private List<Message> initMessages() {
        messages.add(new Message().setStory("Hello Nicolay!").setSender(getInterlocutor()).setTime(System.currentTimeMillis()));
        messages.add(new Message().setStory("Very long message here, it is the longest known message! Piu!").setSender(getInterlocutor()).setTime(System.currentTimeMillis()));
        messages.add(new Message().setStory("Piu!").setSender(getInterlocutor()).setTime(System.currentTimeMillis()));
        messages.add(new Message().setStory("Piu!").setSender(getInterlocutor()).setTime(System.currentTimeMillis()));
        messages.add(new Message().setStory("Nice to meet you Marina!").setSender(getProfile()).setTime(System.currentTimeMillis()));
        messages.add(new Message().setStory("Very very long message here, yup, it is the longest known message! Pau!").setSender(getProfile()).setTime(System.currentTimeMillis()));
        messages.add(new Message().setStory("Pau!").setSender(getProfile()).setTime(System.currentTimeMillis()));
        messages.add(new Message().setStory("Pau!").setSender(getProfile()).setTime(System.currentTimeMillis()));

        return messages;
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
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
