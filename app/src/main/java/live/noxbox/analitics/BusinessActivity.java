package live.noxbox.analitics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;
import java.util.Map;

import live.noxbox.database.Firestore;

import static live.noxbox.database.AppCache.profile;
import static live.noxbox.tools.DateTimeFormatter.date;

public class BusinessActivity extends AppCompatActivity {

    public static SharedPreferences writes;
    public static SharedPreferences reads;
    private static Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        writes = getSharedPreferences("writes", Context.MODE_PRIVATE);
        reads = getSharedPreferences("reads", Context.MODE_PRIVATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences writes = getApplicationContext().getSharedPreferences("writes", MODE_PRIVATE);
        writes.edit().putLong("writes", writes.getLong("writes", 0L) + Firestore.writes).apply();
        Firestore.writes = 0L;

        SharedPreferences reads = getApplicationContext().getSharedPreferences("reads", MODE_PRIVATE);
        reads.edit().putLong("reads", reads.getLong("reads", 0L) + Firestore.reads).apply();
        Firestore.reads = 0L;
    }

    public static void businessEvent(BusinessEvent event) {
        Map<String, String> params = new HashMap<>();
        switch (event) {
            case outBox:
            case inBox:
                params.put("amount", profile().getWallet().getBalance());
                break;
            case chatting:
                params.put("messages", profile().getCurrent().getMessages(profile().getId()).size() + "");
                break;
            case invalidPhoto:
                params.put("issue", profile().getAcceptance().getInvalidAcceptance().name());
                break;
            case post:
                params.put("type", profile().getCurrent().getType().name());
                params.put("role", profile().getCurrent().getRole().name());
                params.put("price", profile().getCurrent().getPrice());
                break;
            case complete:
                params.put("type", profile().getCurrent().getType().name());
                params.put("role", profile().getCurrent().getRole().name());
                params.put("price", profile().getCurrent().getPrice());
                params.put("timeCompleted", date(profile().getCurrent().getTimeCompleted()));
                params.put("writes", writes.getLong("writes", 0L) + Firestore.writes + "");
                params.put("reads", reads.getLong("reads", 0L) + Firestore.reads + "");

                writes.edit().putLong("writes", 0L).apply();
                reads.edit().putLong("writes", 0L).apply();
                Firestore.writes = 0;
                Firestore.reads = 0;
                break;

        }

        businessEvent(event, params);
    }

    public static void businessEvent(BusinessEvent event, Map<String, String> params) {
        if (context == null) return;

        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> param : params.entrySet()) {
            bundle.putString(param.getKey(), param.getValue());
        }
        FirebaseAnalytics.getInstance(context).logEvent(event.name(), bundle);
    }


}
