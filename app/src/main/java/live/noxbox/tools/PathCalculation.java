package live.noxbox.tools;


import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import live.noxbox.R;
import live.noxbox.model.Noxbox;
import live.noxbox.model.TravelMode;

public class PathCalculation {
    public static GoogleMap googleMap;

    public static void createRequestPoints(Noxbox noxbox, GoogleMap googleMap, Activity activity) {
        PathCalculation.googleMap = googleMap;
        if (noxbox.getOwner().getTravelMode() == TravelMode.none) {
            noxbox.getParty().setTravelMode(TravelMode.walking);
            MarkerCreator.createPositionMarker(noxbox.getParty(), noxbox.getParty().getPosition().toLatLng(), googleMap);
            MarkerCreator.createCustomMarker(noxbox, noxbox.getOwner(), googleMap, activity);
            createPathBetweenPoints(noxbox.getPosition().toLatLng(), noxbox.getParty().getPosition().toLatLng());
            ((TextView) activity.findViewById(R.id.requestTravelTime)).setText(calculateTravelTime(noxbox.getParty().getPosition().toLatLng(), noxbox.getPosition().toLatLng(), TravelMode.walking, activity));
        } else if (noxbox.getOwner().getTravelMode() != TravelMode.none) {
            MarkerCreator.createPositionMarker(noxbox.getOwner(), noxbox.getPosition().toLatLng(), googleMap);
            MarkerCreator.createCustomMarker(noxbox, noxbox.getParty(), googleMap, activity);
            createPathBetweenPoints(noxbox.getPosition().toLatLng(), noxbox.getParty().getPosition().toLatLng());
            ((TextView) activity.findViewById(R.id.requestTravelTime)).setText(calculateTravelTime(noxbox.getParty().getPosition().toLatLng(), noxbox.getPosition().toLatLng(), noxbox.getOwner().getTravelMode(), activity));
        }

    }

    private static String calculateTravelTime(LatLng start, LatLng end, TravelMode travelMode, Activity activity) {
        float[] results = new float[1];

        Location.distanceBetween(
                start.latitude,
                start.longitude,
                end.latitude,
                end.longitude,
                results);

        int minutes = (int) (results[0] / travelMode.getSpeedInMetersPerMinute());
        String timeTxt;
        switch (minutes % 10) {
            case 11: {
                timeTxt = activity.getResources().getString(R.string.minutes);
                break;
            }
            case 1: {
                timeTxt = activity.getResources().getString(R.string.minute);
                break;
            }
            case 2: {
                timeTxt = activity.getResources().getString(R.string.minutes_);
                break;
            }
            case 3: {
                timeTxt = activity.getResources().getString(R.string.minutes_);
                break;
            }
            case 4: {
                timeTxt = activity.getResources().getString(R.string.minutes_);
                break;
            }
            default: {
                timeTxt = activity.getResources().getString(R.string.minutes);
                break;
            }
        }
        return String.valueOf(minutes) + " " + timeTxt;
    }

    private static void createPathBetweenPoints(LatLng start, LatLng end) {
        String url = getMapsApiDirectionsUrl(start, end);
        ReadTask readTask = new ReadTask();
        readTask.execute(url);
        // Start downloading json data from Google Directions API

    }

    public static class ReadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // Auto-generated method stub
            String data = "";
            try {
                MapHttpConnection http = new MapHttpConnection();
                data = http.readUr(url[0]);


            } catch (Exception e) {
                // handle exception
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }

    }

    private static String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;

    }

    private static class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            //Auto-generated method stub
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(4);
                polyLineOptions.color(Color.BLUE);
            }

            googleMap.addPolyline(polyLineOptions);

        }
    }

    public static class MapHttpConnection {
        public String readUr(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();


            } catch (Exception e) {
                Log.d("Exception reading url", e.toString());
            } finally {
                istream.close();
                urlConnection.disconnect();
            }
            return data;

        }
    }

    public static class PathJSONParser {

        public static List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;
            try {
                jRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat",
                                        Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng",
                                        Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;

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
