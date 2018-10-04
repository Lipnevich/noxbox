package live.noxbox.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;

public class NavigatorManager {

    public static void openNavigator(Context context, Profile profile) {
        String uri = "google.navigation:"
                + "q="
                + profile.getCurrent().getPosition().getLatitude()
                + ", "
                + profile.getCurrent().getPosition().getLongitude();
        if (profile.getTravelMode() == TravelMode.bicycling) {
            uri = uri.concat("&mode=b");
        }
        if (profile.getTravelMode() == TravelMode.driving) {
            uri = uri.concat("&mode=d");
        }
        if (profile.getTravelMode() == TravelMode.walking) {
            uri = uri.concat("&mode=w");
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                .setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            uri = "yandexnavi://build_route_on_map?"
                    + "lat_from="
                    + profile.getPosition().getLatitude()
                    + "&lon_from="
                    + profile.getPosition().getLongitude()
                    + "&lat_to="
                    + profile.getCurrent().getPosition().getLatitude()
                    + "&lon_to="
                    + profile.getCurrent().getPosition().getLongitude();
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri)).setPackage("ru.yandex.yandexnavi");
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {

                if (System.currentTimeMillis() % 2 == 0) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.google.android.apps.maps")));
                } else {
                    context.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=ru.yandex.yandexnavi")));
                }
            }
        }
    }
}
