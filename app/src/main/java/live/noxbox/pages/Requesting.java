package live.noxbox.pages;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.state.State;
import live.noxbox.tools.TravelCalculation;

import static live.noxbox.state.ProfileStorage.fireProfile;

public class Requesting implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private ObjectAnimator anim;
    private AnimationDrawable animationDrawable;

    public Requesting(GoogleMap googleMap, Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.blinkingInfoLayout).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.cancelButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.getCurrent().setTimeRequested(null);
                clear();
                fireProfile();
            }
        });
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.exchange_rate).setVisibility(View.GONE);

        moveCamera(profile.getCurrent().getPosition().toLatLng(), 15);

        anim = ObjectAnimator.ofInt(activity.findViewById(R.id.circular_progress_bar), "progress", 0, 100);
        anim.setDuration(15000);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();

        animationDrawable = (AnimationDrawable) activity.findViewById(R.id.blinkingInfoLayout).getBackground();
        animationDrawable.setEnterFadeDuration(600);
        animationDrawable.setExitFadeDuration(1200);
        animationDrawable.start();

        profile.getViewed().setParty(profile);

        TravelCalculation.createRequestPoints(profile.getCurrent(), googleMap, activity);
    }


    @Override
    public void clear() {
        googleMap.clear();
        activity.findViewById(R.id.cancelButton).setVisibility(View.GONE);
        activity.findViewById(R.id.blinkingInfoLayout).setVisibility(View.GONE);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.exchange_rate).setVisibility(View.VISIBLE);
        if (anim != null && animationDrawable != null) {
            anim.cancel();
            animationDrawable.stop();
        }

    }

    private void moveCamera(LatLng latLng, float zoom) {
        CameraPosition cameraPosition
                = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);
    }

//    public String requestDirection(String requestUrl) throws IOException {
//        String responseString = "";
//        InputStream inputStream = null;
//        HttpURLConnection httpURLConnection = null;
//        try {
//            URL url = new URL(requestUrl);
//            httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.connect();
//
//            inputStream = httpURLConnection.getInputStream();
//            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//            StringBuffer stringBuffer = new StringBuffer();
//            String line = "";
//            while ((line = bufferedReader.readLine()) != null) {
//                stringBuffer.append(line);
//            }
//            responseString = stringBuffer.toString();
//            bufferedReader.close();
//            inputStreamReader.close();
//        } catch (Exception e) {
//            Log.e("requestDirection", e.getMessage());
//        } finally {
//            if (inputStream != null) {
//                inputStream.close();
//            }
//            httpURLConnection.disconnect();
//        }
//        return responseString;
//    }
//
//    public class TaskRequestDirections extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... strings) {
//            String responseString = "";
//            try {
//                responseString = requestDirection(strings[0])
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return responseString;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            //JSOn parser
//        }
//    }

}
