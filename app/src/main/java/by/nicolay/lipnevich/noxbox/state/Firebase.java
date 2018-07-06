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
package by.nicolay.lipnevich.noxbox.state;

import android.support.annotation.NonNull;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.tools.MessagingService;
import by.nicolay.lipnevich.noxbox.tools.Task;
import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.iid.FirebaseInstanceId;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.reflect.Modifier.isStatic;

public class Firebase {

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    private static DatabaseReference profiles() {
        return db().child("profiles");
    }

    private static DatabaseReference events() {
        return db().child("events");
    }

    private static DatabaseReference requests() {
        return db().child("requests");
    }

    private static GeoFire geo;
    public static GeoFire geo() {
        if(geo == null) geo = new GeoFire(db().child("geo"));
        return geo;
    }

    private static DatabaseReference db() {
        return FirebaseDatabase.getInstance().getReference();
    }

    static Profile profile;

    static Profile getProfile() {
        return profile;
    }

    public static void sendRequest(Request request) {
        requests().child(getProfile().getId()).child(request.getType().toString())
                .setValue(objectToMap(request.setId(getProfile().getId())
                        .setPush(MessagingService.generatePush(request))));
    }

    // Noxboxes API
    private static DatabaseReference currentNoxboxReference() {
        return profiles().child(getProfile().getId()).child("profile").child("current");
    }

    public static void updateCurrentNoxbox(@NonNull Noxbox noxbox) {
        currentNoxboxReference().updateChildren(objectToMap(noxbox));
        getProfile().setCurrent(noxbox);
    }

    public static void removeCurrentNoxbox() {
        currentNoxboxReference().removeValue();
        getProfile().setCurrent(null);
    }

    // History API
    public static Noxbox persistHistory() {
        Noxbox current = getProfile().getCurrent();
        if(current == null) return null;

        like();
        return persistHistory(current.setTimeCompleted(System.currentTimeMillis()));
    }

    public static Noxbox persistHistory(Noxbox noxbox) {
        history().child(noxbox.getId()).updateChildren(objectToMap(noxbox));
        return noxbox;
    }

    public static void loadHistory(final Task<Collection<Noxbox>> task) {
        history().limitToLast(15).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, Noxbox>> genericTypeIndicator
                            = new GenericTypeIndicator<Map<String, Noxbox>>() {};
                    Map<String, Noxbox> history = dataSnapshot.getValue(genericTypeIndicator);
                    task.execute(history.values());
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private static DatabaseReference history() {
        return profiles().child(getProfile().getId()).child("history");
    }

    static private ValueEventListener listener;

    // Profiles API
    static void listenProfile(@NonNull final Task<Profile> task) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profile = snapshot.getValue(Profile.class);
                    refreshNotificationToken();
                    task.execute(profile);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        profiles().child(firebaseUser.getUid()).child("profile").addValueEventListener(listener);
    }

    public static void refreshNotificationToken() {
        String notificationToken = FirebaseInstanceId.getInstance().getToken();
        if(notificationToken == null || getProfile() == null) return;
        if(!notificationToken.equals(getProfile().getNotificationKeys().getAndroid())) {
            getProfile().getNotificationKeys().setAndroid(notificationToken);
            profiles().child(getProfile().getId()).child("notificationKeys")
                    .child("android").setValue(notificationToken);
        }
    }

    public static void updateProfile(Profile profile) {
        profiles().child(profile.getId()).child("profile").updateChildren(objectToMap(profile));
    }

    // in case of null value - does not override value
    private static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> params = new HashMap<>();
        Class clazz = object.getClass();
        while(clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(object);
                        if (value != null) {
                            params.put(field.getName(), value);
                        }
                    } catch (IllegalAccessException e) {
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return params;
    }

    public static void like() {
        getProfile().getRating().getReceived().setLikes(getProfile().getRating().getReceived().getLikes() + 1);
        getProfile().getRating().getSent().setLikes(getProfile().getRating().getSent().getLikes() + 1);
    }
}
