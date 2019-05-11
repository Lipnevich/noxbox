package live.noxbox.analitics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.math.BigDecimal;

import live.noxbox.database.Firestore;
import live.noxbox.debug.DebugMessage;

import static live.noxbox.database.AppCache.profile;

public class BusinessActivity extends AppCompatActivity {

    public static SharedPreferences analytics;
    private static Context context;
    private static final String writes = "writes";
    private static final String reads = "reads";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        analytics = getSharedPreferences("analytics", Context.MODE_PRIVATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        analytics.edit().putLong(writes, analytics.getLong(writes, 0L) + Firestore.writes).apply();
        Firestore.writes = 0L;

        analytics.edit().putLong(reads, analytics.getLong(reads, 0L) + Firestore.reads).apply();
        Firestore.reads = 0L;
    }

    public static void businessEvent(BusinessEvent event, Object... args) {
        // if (context == null || BuildConfig.DEBUG) return;

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
                bundle.putDouble("price", new BigDecimal(profile().getCurrent().getPrice()).doubleValue());
                break;
            case complete:
                bundle.putString("type", profile().getCurrent().getType().name());
                bundle.putString("role", profile().getCurrent().getRole().name());
                bundle.putDouble("price", new BigDecimal(profile().getCurrent().getPrice()).doubleValue());
                bundle.putDouble("timeSpent", (profile().getCurrent().getTimeCompleted() - Math.max(profile().getCurrent().getTimeOwnerVerified(), profile().getCurrent().getTimePartyVerified()) / (1000 * 10)));

                long allWrites = analytics.getLong(writes, 0L) + Firestore.writes;
                long allReads = analytics.getLong(reads, 0L) + Firestore.reads;
                bundle.putLong(writes, allWrites);
                bundle.putLong(reads, allReads);

                DebugMessage.popup(context,
                        reads + allReads + "|" + writes + allWrites, Toast.LENGTH_LONG);

                analytics.edit().putLong(writes, 0L).apply();
                analytics.edit().putLong(reads, 0L).apply();
                Firestore.writes = 0;
                Firestore.reads = 0;
                break;
            case like:
            case dislike:
                bundle.putString("noxboxId", args[0].toString());
                bundle.putString("noxboxType", args[1].toString());
                bundle.putDouble("price", new BigDecimal(args[2].toString()).doubleValue());
                break;

        }

        FirebaseAnalytics.getInstance(context).logEvent(event.name(), bundle);
    }

    public static void businessEvent(String promotionEvent) {
        FirebaseAnalytics.getInstance(context).logEvent(promotionEvent, new Bundle());
    }

}
