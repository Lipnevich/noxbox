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
package live.noxbox.old;

public abstract class PerformerLocationFunction extends ChatFunction {

    /*private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            getProfile().setPosition(from(location));

            Noxbox noxbox = getProfile().getCurrent();
            if(noxbox != null) {
                noxbox.setPerformer(getProfile().publicInfo());
                Firebase.updateCurrentNoxbox(noxbox);
                sendNoxboxEvent(new Message().setType(move));
            }
        }

        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(String provider) {}
        @Override public void onProviderDisabled(String provider) {}
    };

    @Override
    public void processNoxbox(Noxbox noxbox) {
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
    public void prepareForIteration() {
        super.prepareForIteration();
        if(getCurrentNoxbox() != null &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.removeUpdates(locationListener);
        }
    }
*/
}
