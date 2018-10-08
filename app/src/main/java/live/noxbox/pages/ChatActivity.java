package live.noxbox.pages;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.model.Message;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.Task;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;
import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;
import static java.util.Collections.sort;

public class ChatActivity extends AppCompatActivity {

    public static final int CODE = 1001;

    private List<Message> messages = new ArrayList<>();
    private RecyclerView chatList;
    private TextView text;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long millis = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chat);

        chatList = findViewById(R.id.chat_list);
        chatList.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        chatList.setLayoutManager(manager);

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

        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
            }
        });
    }

    private void draw(final Profile profile) {
        final DisplayMetrics screen = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(screen);

        TextView interlocutorName = findViewById(R.id.chat_opponent_name);
        interlocutorName.setText(getInterlocutorName(profile));

        initMessages(profile);
        chatAdapter = new ChatAdapter(screen, messages, profile.getId());
        chatList.setAdapter(chatAdapter);
        chatList.smoothScrollToPosition(View.FOCUS_DOWN);

        text = findViewById(R.id.type_message);
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    send(profile);
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(profile);
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

    private void send(Profile profile) {
        if(TextUtils.isEmpty(text.getText().toString().trim())) return;
        add(new Message().setWasRead(true));
        text.setText("");
    }

    private void add(Message message) {
        sound();
        messages.add(message);
        sort(messages);
        chatAdapter.notifyItemInserted(messages.indexOf(message));

        chatList.scrollToPosition(messages.size() - 1);
    }

    private void sound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(ChatActivity.this, R.raw.message);
        mediaPlayer.start();
    }

    private List<Message> initMessages(Profile profile) {
        messages.clear();

        if(profile.getCurrent() != null) {
            messages.addAll(profile.getCurrent().getChat().values());
            sort(messages);
        }

        return messages;
    }

    private String getInterlocutorName(@NonNull Profile profile) {
        return profile.getCurrent().getNotMe(profile.getId()).getName();
    }

}
