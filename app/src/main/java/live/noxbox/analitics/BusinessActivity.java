package live.noxbox.analitics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import live.noxbox.BuildConfig;
import live.noxbox.database.Firestore;

import static live.noxbox.database.AppCache.profile;

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
        if (context == null || BuildConfig.DEBUG) return;

        Bundle bundle = new Bundle();

        switch (event) {
            case outBox:
            case inBox:
                bundle.putDouble("amount", Double.valueOf(profile().getWallet().getBalance()));
                break;
            case cancel:
            case chatting:
                bundle.putInt("messages", profile().getCurrent().getMessages(profile().getId()).size());
                break;
            case invalidPhoto:
                bundle.putString("issue", profile().getAcceptance().getInvalidAcceptance().name());
                break;
            case post:
                bundle.putString("type", profile().getCurrent().getType().name());
                bundle.putString("role", profile().getCurrent().getRole().name());
                bundle.putDouble("price", Double.valueOf(profile().getCurrent().getPrice()));
                break;
            case complete:
                bundle.putString("type", profile().getCurrent().getType().name());
                bundle.putString("role", profile().getCurrent().getRole().name());
                bundle.putDouble("price", Double.valueOf(profile().getCurrent().getPrice()));
                bundle.putDouble("timeSpent", (profile().getCurrent().getTimeCompleted() - Math.max(profile().getCurrent().getTimeOwnerVerified(), profile().getCurrent().getTimePartyVerified())/(1000 * 10)));
                bundle.putLong("writes", writes.getLong("writes", 0L) + Firestore.writes);
                bundle.putLong("reads", reads.getLong("reads", 0L) + Firestore.reads);

                writes.edit().putLong("writes", 0L).apply();
                reads.edit().putLong("writes", 0L).apply();
                Firestore.writes = 0;
                Firestore.reads = 0;
                break;

        }

        FirebaseAnalytics.getInstance(context).logEvent(event.name(), bundle);
    }

}
