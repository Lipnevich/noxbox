package by.nicolay.lipnevich.noxbox.pages;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import java.util.List;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.UserType;
import by.nicolay.lipnevich.noxbox.payer.massage.BuildConfig;
import by.nicolay.lipnevich.noxbox.payer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.TimeLog;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getUserType;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNoxboxInProgress;
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

        messages.add(new Message().setStory(text.getText().toString()).setSender(getProfile()).setTime(System.currentTimeMillis()));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatList.scrollToPosition(messages.size() - 1);
        text.setText("");
    }

    private List<Message> initMessages() {
        if(tryGetNoxboxInProgress() != null) {
            messages.addAll(tryGetNoxboxInProgress().getChat().values());
        }

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
