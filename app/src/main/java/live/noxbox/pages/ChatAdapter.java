package live.noxbox.pages;

import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import live.noxbox.R;
import live.noxbox.model.Event;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Event> events;
    private String profileId;
    private DisplayMetrics metrics;

    public ChatAdapter(DisplayMetrics metrics, List<Event> events, String profileId) {
        this.events = events;
        this.profileId = profileId;
        this.metrics = metrics;
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private boolean isOwner(int position) {
        return events.get(position).getSender().getId().equals(profileId);
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);

        CardView card = v.findViewById(R.id.chat_card);
        if(!isOwner(position)) {
            v.setHorizontalGravity(Gravity.START);
            card.setCardBackgroundColor(parent.getResources().getColor(R.color.message));
        }

        Event event = events.get(position);
        event.setWasRead(true);
        TextView text = v.findViewById(R.id.message_id);
        text.setText(event.getMessage());
        TextView time = v.findViewById(R.id.time_id);
        time.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(event.getTime())));

        LayoutParams cardParams = (LayoutParams) card.getLayoutParams();
        if(position == 0) {
            cardParams.topMargin *= 5;
            card.setLayoutParams(cardParams);
        }

        if(isTooLong(text)) {
            cardParams.width = 0;
            cardParams.weight = 90;
            card.setLayoutParams(cardParams);

            ((LinearLayout)v.findViewById(R.id.chat_card_layout)).setOrientation(LinearLayout.VERTICAL);

            time.setGravity(Gravity.END);
            text.setPadding(0, 18, 0, -2);
            time.setPadding(0,-2,0, 18);
            LayoutParams timeParams = (LayoutParams) time.getLayoutParams();
            timeParams.topMargin = 0;
            time.setLayoutParams(timeParams);
        }

        return new ViewHolder(v);
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

