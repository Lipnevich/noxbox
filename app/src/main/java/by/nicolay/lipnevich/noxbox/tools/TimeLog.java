package by.nicolay.lipnevich.noxbox.tools;

import android.content.Context;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import by.nicolay.lipnevich.noxbox.payer.massage.BuildConfig;

public final class TimeLog {

    public enum Event {
        inflateMessage,
        ownerMessage,
        formatMessage,
        bigMessage,
        onCreateChat,
        onChatOpen, openHistory, openedHistory;
    }

    private static final Map<Event, AtomicLong> timeLog = new HashMap<>();
    static {
        for(Event event : Event.values()) {
            timeLog.put(event, new AtomicLong(0));
        }
    }

    public static Long log(Long millis, Event event, Context context) {
        long spent = System.currentTimeMillis() - millis;
        timeLog.get(event).addAndGet(spent);
        if(BuildConfig.DEBUG) {
            Toast.makeText(context, spent + " millis was spent on " + event.name(), Toast.LENGTH_SHORT).show();
        }
        return System.currentTimeMillis();
    }


}
