package live.noxbox.tools;


import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import live.noxbox.R;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.TravelMode;

import static live.noxbox.MapActivity.dpToPx;
import static live.noxbox.model.TravelMode.none;

public class PathFinder {


    public static void createRequestPoints(Noxbox noxbox, GoogleMap googleMap, Activity activity) {
        if (noxbox.getRole() == MarketRole.supply) {//исполнитель
            if (noxbox.getOwner().getTravelMode() == none) {
                MarkerCreator.createPositionMarker(noxbox.getParty().getTravelMode(), noxbox.getParty().getPosition().toLatLng(), googleMap);
                MarkerCreator.createCustomMarker(noxbox, googleMap, activity, noxbox.getOwner().getTravelMode());
                createPathBetweenPoints(noxbox.getParty().getPosition(), noxbox.getPosition(), noxbox.getParty().getTravelMode(), activity, googleMap);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder().include(noxbox.getParty().getPosition().toLatLng()).include(noxbox.getPosition().toLatLng()).build(), dpToPx(68)));

            } else {
                if (noxbox.getOwner().getHost() && !noxbox.getParty().getHost()) {
                    MarkerCreator.createPositionMarker(noxbox.getParty().getTravelMode(), noxbox.getParty().getPosition().toLatLng(), googleMap);
                    MarkerCreator.createCustomMarker(noxbox, googleMap, activity, TravelMode.none);
                    createPathBetweenPoints(noxbox.getParty().getPosition(), noxbox.getPosition(), noxbox.getParty().getTravelMode(), activity, googleMap);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder().include(noxbox.getParty().getPosition().toLatLng()).include(noxbox.getPosition().toLatLng()).build(), dpToPx(68)));
                } else {
                    MarkerCreator.createPositionMarker(TravelMode.none, noxbox.getParty().getPosition().toLatLng(), googleMap);
                    MarkerCreator.createCustomMarker(noxbox, googleMap, activity, noxbox.getOwner().getTravelMode());
                    createPathBetweenPoints(noxbox.getPosition(), noxbox.getParty().getPosition(), TravelMode.none, activity, googleMap);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder().include(noxbox.getParty().getPosition().toLatLng()).include(noxbox.getPosition().toLatLng()).build(), dpToPx(68)));
                }

            }

        } else if (noxbox.getRole() == MarketRole.demand) {
            if (noxbox.getOwner().getTravelMode() == none) {
                MarkerCreator.createPositionMarker(noxbox.getParty().getTravelMode(), noxbox.getParty().getPosition().toLatLng(), googleMap);
                MarkerCreator.createCustomMarker(noxbox, googleMap, activity, noxbox.getOwner().getTravelMode());
                createPathBetweenPoints(noxbox.getParty().getPosition(), noxbox.getPosition(), noxbox.getParty().getTravelMode(), activity, googleMap);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder().include(noxbox.getParty().getPosition().toLatLng()).include(noxbox.getPosition().toLatLng()).build(), dpToPx(68)));
            } else {
                if (noxbox.getParty().getTravelMode() == none && noxbox.getParty().getHost()) {
                    MarkerCreator.createPositionMarker(noxbox.getParty().getTravelMode(), noxbox.getParty().getPosition().toLatLng(), googleMap);
                    MarkerCreator.createCustomMarker(noxbox, googleMap, activity, noxbox.getOwner().getTravelMode());
                    createPathBetweenPoints(noxbox.getPosition(), noxbox.getParty().getPosition(), noxbox.getParty().getTravelMode(), activity, googleMap);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder().include(noxbox.getParty().getPosition().toLatLng()).include(noxbox.getPosition().toLatLng()).build(), dpToPx(68)));
                } else {
                    MarkerCreator.createPositionMarker(noxbox.getParty().getTravelMode(), noxbox.getParty().getPosition().toLatLng(), googleMap);
                    MarkerCreator.createCustomMarker(noxbox, googleMap, activity, TravelMode.none);
                    createPathBetweenPoints(noxbox.getParty().getPosition(), noxbox.getPosition(), noxbox.getParty().getTravelMode(), activity, googleMap);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder().include(noxbox.getParty().getPosition().toLatLng()).include(noxbox.getPosition().toLatLng()).build(), dpToPx(68)));
                }
            }
        }
    }

    private static final String key = "AIzaSyArShVxHFrGDuU_mTVMddB1ToPTsMjjrb0";

    private static void createPathBetweenPoints(Position start, Position end, TravelMode travelMode, Activity activity, GoogleMap googleMap) {
        new ParserTask(activity, googleMap).execute(String.format(Locale.US, "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=%f,%f&destination=%f,%f&sensor=false&mode=%s&alternatives=false&key=%s",
                start.getLatitude(),
                start.getLongitude(),
                end.getLatitude(),
                end.getLongitude(),
                travelMode.toString(),
                key));
        // Start downloading json data from Google Directions API
    }

    private static class Path {
        int timeInMinutes;
        List<LatLng> points = new ArrayList<>();
    }

    private static class ParserTask extends AsyncTask<String, Integer, Path> {
        Activity activity;
        GoogleMap googleMap;

        public ParserTask(Activity activity, GoogleMap googleMap) {
            this.activity = activity;
            this.googleMap = googleMap;
        }

        @Override
        protected Path doInBackground(String... jsonData) {
            try {
                return parse(readUrl(jsonData[0]));
            } catch (JSONException | IOException e) {
                Crashlytics.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Path path) {
            if (path == null) return;
            PolylineOptions polyLineOptions = new PolylineOptions();

            polyLineOptions.addAll(path.points);
            polyLineOptions.width(7);
            polyLineOptions.color(activity.getResources().getColor(R.color.primary));

            googleMap.addPolyline(polyLineOptions);


            String timeTxt;
            switch (path.timeInMinutes % 10) {
                case 1:
                    timeTxt = activity.getResources().getString(R.string.minute);
                    break;

                case 2:
                case 3:
                case 4:
                    timeTxt = activity.getResources().getString(R.string.minutes_);
                    break;

                default:
                    timeTxt = activity.getResources().getString(R.string.minutes);
            }
            ((TextView) activity.findViewById(R.id.travelTime)).setText(path.timeInMinutes + " " + timeTxt);
            activity.findViewById(R.id.timeLayout).setVisibility(View.VISIBLE);

        }

        private static String readUrl(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            BufferedReader br = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                br = new BufferedReader(new InputStreamReader(istream, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();

            } finally {
                if (br != null) {
                    br.close();
                }
                if (istream != null) {
                    istream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return data;
        }


        private static Path parse(String response) throws JSONException {
            Path path = new Path();
            JSONObject route = new JSONObject(response).getJSONArray("routes").getJSONObject(0);

            JSONObject overviewPolylines = route.getJSONObject("overview_polyline");
            path.points.addAll(decodePoly(overviewPolylines.getString("points")));
            Integer estimation = route.getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
            path.timeInMinutes = estimation / 60;

            return path;
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

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }

    }
}