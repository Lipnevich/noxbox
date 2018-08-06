package by.nicolay.lipnevich.noxbox.tools;


import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;

import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.TravelMode;

public class TravelCalculation {

    public static void createRequestPoints(Noxbox noxbox, GoogleMap googleMap, Activity activity) {
        if (noxbox.getOwner().getTravelMode() == TravelMode.none) {
            MarkerCreator.createPositionMarker(noxbox.getParty(),noxbox.getParty().getPosition().toLatLng(),googleMap);
            MarkerCreator.createCustomMarker(noxbox,noxbox.getOwner(),googleMap,activity);
        } else if (noxbox.getOwner().getTravelMode() != TravelMode.none) {
            MarkerCreator.createPositionMarker(noxbox.getOwner(),noxbox.getPosition().toLatLng(),googleMap);
            MarkerCreator.createCustomMarker(noxbox,noxbox.getParty(),googleMap,activity);
        }

        //createPathBetweenPoints(noxbox);
    }

    private static void createPathBetweenPoints(final Noxbox noxbox) {
    }

}
