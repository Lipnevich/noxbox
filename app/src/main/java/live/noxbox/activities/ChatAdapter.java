package live.noxbox.activities;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.model.Message;
import live.noxbox.model.Profile;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Message> messages;
    private DisplayMetrics metrics;
    private Context context;
    private Profile profile;
    private Long wasRead = 0L;
    private boolean needToUpdateNoxbox = false;


    public ChatAdapter(DisplayMetrics metrics, List<Message> messages, Context context, Profile profile) {
        this.messages = messages;
        this.metrics = metrics;
        this.context = context;
        this.profile = profile;
        if (profile.getCurrent().getOwner().equals(profile)) {
            wasRead = profile.getCurrent().getChat().getPartyReadTime();
        } else {
            wasRead = profile.getCurrent().getChat().getOwnerReadTime();
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private boolean isMyMessage(int position) {
        return messages.get(position).isMyMessage();
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        LinearLayout rootLayout = viewHolder.rootLayout;
        CardView messageCard = viewHolder.messageCard;
        LinearLayout innerLayout = viewHolder.innerLayout;
        TextView messageView = viewHolder.message;
        TextView timeView = viewHolder.time;
        ImageView checkedView = viewHolder.checked;

        if (!isMyMessage(position)) {
            rootLayout.setGravity(Gravity.START);
            messageCard.setCardBackgroundColor(context.getResources().getColor(R.color.message));
        }

        Message message = messages.get(position);

        messageView.setText(message.getMessage());
        timeView.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(message.getTime())));

        LayoutParams cardParams = (LayoutParams) messageCard.getLayoutParams();
        if (position == 0) {
            cardParams.topMargin *= 5;
            messageCard.setLayoutParams(cardParams);
        }

        if (isTooLong(messageView)) {
            cardParams.width = 0;
            cardParams.weight = 90;
            messageCard.setLayoutParams(cardParams);

            innerLayout.setOrientation(LinearLayout.VERTICAL);

            timeView.setGravity(Gravity.END);
            messageView.setPadding(0, 18, 0, -2);
            timeView.setPadding(0, -2, 0, 18);
            LayoutParams timeParams = (LayoutParams) timeView.getLayoutParams();
            timeParams.topMargin = 0;
            timeView.setLayoutParams(timeParams);
        }

        if (messages.size() - 1 == position) {
            Message lastNotMyMessage = null;
            for (Message m : messages) {
                if (!m.isMyMessage()) {
                    lastNotMyMessage = m;
                }
            }

            if (profile.getCurrent().getOwner().equals(profile)) {
                wasRead = profile.getCurrent().getChat().getPartyReadTime();
                if (lastNotMyMessage != null && lastNotMyMessage.getTime() > profile.getCurrent().getChat().getOwnerReadTime()) {
                    profile.getCurrent().getChat().setOwnerReadTime(System.currentTimeMillis());
                    needToUpdateNoxbox = true;
                }
            } else {
                wasRead = profile.getCurrent().getChat().getOwnerReadTime();
                if (lastNotMyMessage != null && lastNotMyMessage.getTime() > profile.getCurrent().getChat().getPartyReadTime()) {
                    profile.getCurrent().getChat().setPartyReadTime(System.currentTimeMillis());
                    needToUpdateNoxbox = true;
                }
            }
        }

        if (isMyMessage(position)) {
            checkedView.setVisibility(View.VISIBLE);
            if (wasRead(message)) {
                checkedView.setColorFilter(context.getResources().getColor(R.color.primary));
            } else {
                checkedView.setColorFilter(context.getResources().getColor(R.color.divider));
            }
        }

        if (needToUpdateNoxbox) {
            AppCache.updateNoxbox();
            needToUpdateNoxbox = false;
        }

    }

    private boolean wasRead(Message message) {
        return message.getTime() < wasRead;
    }

    private boolean isTooLong(TextView text) {
        Paint fontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fontPaint.setTextSize(text.getTextSize());
        fontPaint.setStyle(Paint.Style.STROKE);

        float textWidth = fontPaint.measureText(text.getText().toString() + "00:00");

        return (textWidth / metrics.widthPixels) * 100 > 85;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootLayout;
        CardView messageCard;
        LinearLayout innerLayout;
        TextView message;
        TextView time;
        ImageView checked;

        public ViewHolder(@NonNull View layout) {
            super(layout);
            rootLayout = layout.findViewById(R.id.rootLayout);
            messageCard = layout.findViewById(R.id.chat_card);
            innerLayout = layout.findViewById(R.id.chat_card_layout);
            message = layout.findViewById(R.id.message);
            time = layout.findViewById(R.id.time);
            checked = layout.findViewById(R.id.checked);
        }
    }

}

