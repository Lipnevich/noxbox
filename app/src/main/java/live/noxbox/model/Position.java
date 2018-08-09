package live.noxbox.model;

import android.location.Location;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Position implements Serializable {

    private double latitude;
    private double longitude;

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Position() {
    }

    public double getLongitude() {
        return longitude;
    }

    public Position setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public Position setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }


    public GeoLocation toGeoLocation() {
        return new GeoLocation(latitude, longitude);
    }

    public static Position from(Location location) {
        if (location == null) {
            return null;
        }
        return new Position().setLatitude(location.getLatitude()).setLongitude(location.getLongitude());
    }

    public Location toLocation() {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public static Position from(LatLng latLng) {
        return new Position().setLatitude(latLng.latitude).setLongitude(latLng.longitude);
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String toString() {
        return "Position[" + String.format("%.2f", latitude) + ";" + String.format("%.2f", longitude) + ']';
    }
}
