package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.menu.HistoryActivity;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.COMISSION_FEE;
import static live.noxbox.Configuration.CURRENCY;

public class NotificationComplete extends Notification {
    private String price;

    public NotificationComplete(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

        price = data.get("price");

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_completed);
        contentView.setTextViewText(R.id.content, context.getString(type.getContent()));

        String comission = " " + context.getResources().getString(R.string.withComission) + " " + COMISSION_FEE + " " + CURRENCY;
        contentView.setTextViewText(R.id.content, price.concat(" ").concat(CURRENCY).concat(comission));


        isAlertOnce = true;
        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, HistoryActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        isAutoCancel = true;

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        Firestore.listenProfile(new Task<Profile>() {
            @Override
            public void execute(final Profile profile) {
                Firestore.readNoxbox(noxboxId, new Task<Noxbox>() {
                    @Override
                    public void execute(final Noxbox noxbox) {
                        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
                        if(noxbox.getTimeCompleted() != null){
                            String message;

                            if (noxbox.getOwner().equals(profile)) {
                                if (noxbox.getRole() == MarketRole.supply) {
                                    message = context.getResources().getString(R.string.toEarn).concat(":");
                                } else {
                                    message = context.getResources().getString(R.string.spent).concat(":");
                                }
                            } else {
                                if (noxbox.getRole() == MarketRole.supply) {
                                    message = context.getResources().getString(R.string.toEarn).concat(":");
                                } else {
                                    message = context.getResources().getString(R.string.spent).concat(":");
                                }
                            }

                            contentView.setTextViewText(R.id.contentRole, message);
                            getNotificationService(context).notify(type.getGroup(), builder.build());

                        }
                    }
                });
            }
        });


    }
}
