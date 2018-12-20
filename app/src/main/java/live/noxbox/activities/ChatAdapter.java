package live.noxbox.activities;

import android.content.Context;
import android.graphics.Paint;
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
import live.noxbox.model.Message;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Message> messages;
    private DisplayMetrics metrics;
    private Context context;
    private Long wasRead;


    public ChatAdapter(DisplayMetrics metrics, List<Message> messages, Context context, Long wasRead) {
        this.messages = messages;
        this.metrics = metrics;
        this.context = context;
        this.wasRead = wasRead == null ? 0 : wasRead;
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
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);

        CardView card = v.findViewById(R.id.chat_card);
        if (!isMyMessage(position)) {
            v.setHorizontalGravity(Gravity.START);
            card.setCardBackgroundColor(parent.getResources().getColor(R.color.message));
        }

        Message message = messages.get(position);

        TextView text = v.findViewById(R.id.message_id);
        text.setText(message.getMessage());
        TextView time = v.findViewById(R.id.time_id);
        time.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(message.getTime())));
        ImageView checked = v.findViewById(R.id.checked);

        if (isMyMessage(position)) {
            checked.setVisibility(View.VISIBLE);
            if (wasRead(message)) {
                checked.setColorFilter(context.getResources().getColor(R.color.primary));
            } else {
                checked.setColorFilter(context.getResources().getColor(R.color.divider));
            }
        }else{

        }

        LayoutParams cardParams = (LayoutParams) card.getLayoutParams();
        if (position == 0) {
            cardParams.topMargin *= 5;
            card.setLayoutParams(cardParams);
        }

        if (isTooLong(text)) {
            cardParams.width = 0;
            cardParams.weight = 90;
            card.setLayoutParams(cardParams);

            ((LinearLayout) v.findViewById(R.id.chat_card_layout)).setOrientation(LinearLayout.VERTICAL);

            time.setGravity(Gravity.END);
            text.setPadding(0, 18, 0, -2);
            time.setPadding(0, -2, 0, 18);
            LayoutParams timeParams = (LayoutParams) time.getLayoutParams();
            timeParams.topMargin = 0;
            time.setLayoutParams(timeParams);
        }

        return new ViewHolder(v);
    }

    private boolean wasRead(Message message) {
        return message.getTime() < wasRead;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    }

    private boolean isTooLong(TextView text) {
        Paint fontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fontPaint.setTextSize(text.getTextSize());
        fontPaint.setStyle(Paint.Style.STROKE);

        float textWidth = fontPaint.measureText(text.getText().toString() + "00:00");

        return (textWidth / metrics.widthPixels) * 100 > 85;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout item;

        public ViewHolder(LinearLayout v) {
            super(v);
            item = v;
        }
    }

}

