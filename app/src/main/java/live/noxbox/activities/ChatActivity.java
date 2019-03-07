package live.noxbox.activities;


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.model.Message;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.tools.ImageManager;
import live.noxbox.tools.Router;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;
import static java.util.Collections.sort;
import static live.noxbox.database.AppCache.profile;

public class ChatActivity extends BaseActivity {

    public static final int CODE = 1001;

    private RelativeLayout root;
    private ImageView backgroundImage;
    private TextView chatOpponentName;
    private EditText text;

    private RecyclerView chatList;
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
        backgroundImage = findViewById(R.id.background);
        chatOpponentName = findViewById(R.id.chat_opponent_name);
        text = findViewById(R.id.type_message);
        chatList = findViewById(R.id.chat_list);
        chatList.setHasFixedSize(true);
        chatList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        screen = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(screen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.listenProfile(ChatActivity.class.getName(), profile -> {
            if (messages.size() > 0 && messages.size() == current().getMessages(profile.getId()).size()) {
                Message lastMessage = messages.get(messages.size() - 1);
                if (profile.equals(current().getParty()) && lastMessage.getTime() > current().getChat().getOwnerReadTime()
                        || (profile.equals(current().getOwner()) && lastMessage.getTime() > current().getChat().getPartyReadTime())) {
                    return;
                }
            }

            draw(profile);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(ChatActivity.class.getName());
    }


    private void draw(final Profile profile) {
        drawToolbar(profile);
        drawBackground();
        drawDynamicalChatView(profile);
        drawSendView(profile);
        initChatAdapter(profile);
    }

    private void initChatAdapter(Profile profile) {
        if (chatAdapter == null) {
            chatAdapter = new ChatAdapter(screen, messages, getApplicationContext(), profile);
            chatList.setAdapter(chatAdapter);
        } else {
            chatAdapter.notifyDataSetChanged();
        }
    }

    private void drawToolbar(Profile profile) {
        ImageManager.createCircleProfilePhotoFromUrl(this, current().getNotMe(profile.getId()).getPhoto(), (ImageView) findViewById(R.id.photo));
        findViewById(R.id.homeButton).setOnClickListener(v -> Router.finishActivity(ChatActivity.this));
        chatOpponentName.setText(profile.getCurrent().getNotMe(profile.getId()).getName());
    }


    private void drawBackground() {
        Drawable drawable = getDrawable(AppCache.profile().getCurrent().getType().getIllustration());

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidthDp = size.x;
        int screenHeightDp = size.y;

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidthDp, screenHeightDp);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            backgroundImage.setLayoutParams(layoutParams);
            backgroundImage.setScaleX(2.5F);
            backgroundImage.setScaleY(1.0F);
        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidthDp, screenHeightDp);
            backgroundImage.setLayoutParams(layoutParams);
            backgroundImage.setScaleY(1.0F);
            backgroundImage.setScaleX(1.0F);
        }
        backgroundImage.setBackground(drawable);
    }

    private void drawSendView(Profile profile) {
        text.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                send(profile);
                return true;
            }
            return false;
        });

        findViewById(R.id.send_message).setOnClickListener(v -> send(profile));
    }

    private void drawDynamicalChatView(Profile profile) {
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
        initMessages(profile);

        if (messages != null && messages.size() > 0) {
            chatList.scrollToPosition(messages.size() - 1);
        }
    }


    private void send(Profile profile) {
        String trimmedText = text.getText().toString().trim();
        if (TextUtils.isEmpty(trimmedText)) return;

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
    }


    private List<Message> initMessages(Profile profile) {
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
            sort(messages);
        }
        return messages;
    }

    private void add(Message message) {
        message.setMyMessage(true);
        messages.add(message);
        sort(messages);
        chatAdapter.notifyDataSetChanged();
        chatList.scrollToPosition(messages.size() - 1);
    }

    private Noxbox current() {
        return profile().getCurrent();
    }


    private int height(Bitmap picture, int width, double koef) {
        int height = (int) (width * koef);
        if (picture.getHeight() < height) {
            height = picture.getHeight();
        }
        return height;
    }

    private int width(Bitmap picture, double koef) {
        int width = (int) (picture.getHeight() / koef);
        if (picture.getWidth() < width) {
            width = picture.getWidth();
        }
        return width;
    }

    private Bitmap resize(DisplayMetrics screen, Bitmap picture) {
        return createScaledBitmap(picture, screen.widthPixels, screen.heightPixels, false);
    }

    private Bitmap blur(int blurRadius, Bitmap picture) {
        if (blurRadius == 0) return picture;

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
}
