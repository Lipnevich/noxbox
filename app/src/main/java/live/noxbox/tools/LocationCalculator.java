package live.noxbox.tools;

import android.location.Location;
import android.util.Log;

import live.noxbox.model.Position;
import live.noxbox.model.TravelMode;

import static live.noxbox.Constants.MAX_MINUTES;

public class LocationCalculator {

    public static long getTimeInMinutesBetweenUsers(Position ownerPosition, Position partyPosition, TravelMode travelMode) {
        if(travelMode.getSpeedInMetersPerMinute() == 0){
            return MAX_MINUTES;
        }
        int minutes = getDistanceBetweenTwoPoints(ownerPosition, partyPosition) / travelMode.getSpeedInMetersPerMinute();

        return minutes;

    }

    public static int getDistanceBetweenTwoPoints(Position ownerPosition, Position partyPosition) {
        float[] results = new float[1];

        Location.distanceBetween(
                ownerPosition.getLatitude(),
                ownerPosition.getLongitude(),
                partyPosition.getLatitude(),
                partyPosition.getLongitude(),
                results);
        Log.d("DistBetweenTwoPoints", "" + results[0]);

        return (int) results[0];
    }
}
