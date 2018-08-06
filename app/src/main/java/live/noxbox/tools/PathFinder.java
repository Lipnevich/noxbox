package live.noxbox.tools;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import live.noxbox.model.Position;
import live.noxbox.model.TravelMode;

public class PathFinder {

    public static Map.Entry<Integer, List<LatLng>> getPathPoints(final Position performer, final Position payer,
                                                                 TravelMode travelMode, String key) {
        String url = String.format(Locale.US, "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=%f,%f&destination=%f,%f&sensor=false&mode=%s&alternatives=true&key=%s",
                performer.getLatitude(),
                performer.getLongitude(),
                payer.getLatitude(),
                payer.getLongitude(),
                travelMode.toString(),
                key);

        try {
            InputStream is = new URL(url).openConnection().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();

            JSONObject json = new JSONObject(sb.toString());
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            Integer estimation = (Integer) routes.getJSONArray("legs")
                    .getJSONObject(0)
                    .getJSONObject("duration")
                    .get("value");

            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> points = decodePoly(encodedString);

            return Collections.singletonMap(estimation, points).entrySet().iterator().next();
        } catch (IOException | JSONException e) {
            Crashlytics.logException(e);
        }
        return null;
    }

    private static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

}