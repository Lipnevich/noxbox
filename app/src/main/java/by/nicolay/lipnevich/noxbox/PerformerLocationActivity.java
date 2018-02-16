/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package by.nicolay.lipnevich.noxbox;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.tools.Firebase;

import static by.nicolay.lipnevich.noxbox.model.MessageType.move;
import static by.nicolay.lipnevich.noxbox.model.Position.from;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.sendMessageForEveryoneExceptMe;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNoxboxInProgress;

public abstract class PerformerLocationActivity extends NoxboxActivity {

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            getProfile().setPosition(from(location));

            Noxbox noxbox = tryGetNoxboxInProgress();
            if(noxbox != null) {
                noxbox.getPerformers().put(getProfile().getId(), getProfile().publicInfo());
                Firebase.updateCurrentNoxbox(noxbox);
                sendMessageForEveryoneExceptMe(new Message().setType(move));
            }
        }

        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(String provider) {}
        @Override public void onProviderDisabled(String provider) {}
    };

    @Override
    protected void processNoxbox(Noxbox noxbox) {
        super.processNoxbox(noxbox);
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            int minTimeInMillis = 5 * 1000;
            int minDistanceInMeters = 5;
            locationManager.removeUpdates(locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    minTimeInMillis, minDistanceInMeters, locationListener);
        }
    }

    @Override
    protected void prepareForIteration() {
        super.prepareForIteration();
        if(tryGetNoxboxInProgress() != null &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.removeUpdates(locationListener);
        }
    }

}
