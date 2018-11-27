package live.noxbox.tools;

import android.location.Location;
import android.util.Log;

import live.noxbox.model.Position;
import live.noxbox.model.TravelMode;

public class LocationCalculator {

    public static long getTimeInMillisBetweenUsers(Position ownerPosition, Position partyPosition, TravelMode travelMode) {

        int minutes = getDistanceBetweenTwoPoints(ownerPosition, partyPosition) / travelMode.getSpeedInMetersPerMinute();

        return minutes * 60000;

    }

    public static int getDistanceBetweenTwoPoints(Position ownerPosition, Position partyPosition) {
        float[] results = new float[1];

        Location.distanceBetween(
                ownerPosition.getLatitude(),
                ownerPosition.getLongitude(),
                partyPosition.getLatitude(),
                partyPosition.getLongitude(),
                results);
        Log.d("DistnceBetweenTwoPoints", "" + results[0]);

        return (int) results[0];
    }
}
