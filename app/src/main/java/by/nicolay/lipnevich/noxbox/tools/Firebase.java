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
package by.nicolay.lipnevich.noxbox.tools;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.model.Acceptance;
import by.nicolay.lipnevich.noxbox.model.AllRates;
import by.nicolay.lipnevich.noxbox.model.Event;
import by.nicolay.lipnevich.noxbox.model.EventType;
import by.nicolay.lipnevich.noxbox.model.NotificationKeys;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.model.UserAccount;
import by.nicolay.lipnevich.noxbox.model.Wallet;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.sort;

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

    private static UserAccount user;

    public static void sendRequest(Request request) {
        requests().child(getProfile().getId()).child(request.getType().toString())
                .setValue(objectToMap(request.setId(getProfile().getId())
                        .setPush(MessagingService.generatePush(request))));
    }

    // Events API
    public static Event sendEvent(String recipientId, Event event) {
        // events move and story allowed only
        String eventKey = events().child(recipientId).child(event.getType().name()).push().getKey();
        events().child(recipientId).child(event.getType().name()).child(eventKey)
                .updateChildren(objectToMap(event
                .setId(eventKey)
                .setSender(getProfile().publicInfo())
                .setTime(System.currentTimeMillis())
                .setPush(MessagingService.generatePush(event, recipientId))));
        return event;
    }

    public static Event sendNoxboxEvent(Event event) {
        event = sendEvent(getProfile().getCurrent().getParty().getId(), event);
        return event;
    }

    public static void addMessageToChat(Event event) {
        Noxbox noxbox = getProfile().getCurrent();
        if(noxbox != null) {
            noxbox.getChat().put(event.getId(), event);
            updateCurrentNoxbox(noxbox);
        }
    }

    private static ValueEventListener listenerAllEvents;

    public static void listenAllEvents(final Task<Event> task) {
        listenerAllEvents = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, Map<String, Event>>> genericTypeIndicator
                            = new GenericTypeIndicator<Map<String, Map<String, Event>>>() {};
                    Map<String, Map<String, Event>> allEvents = dataSnapshot.getValue(genericTypeIndicator);
                    for(Map.Entry<String, Map<String, Event>> typeEvents : allEvents.entrySet()) {
                        EventType type = EventType.valueOf(typeEvents.getKey());
                        processTypeEvents(type, typeEvents.getValue(), task);
                    }
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        };

        events().child(getProfile().getId()).addValueEventListener(listenerAllEvents);
    }

    public static void listenTypeEvents(final EventType type, final Task<Event> task) {
        if(listenerAllEvents != null) {
            events().child(getProfile().getId()).removeEventListener(listenerAllEvents);
        }
        events().child(getProfile().getId()).child(type.name()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, Event>> genericTypeIndicator
                            = new GenericTypeIndicator<Map<String, Event>>() {};
                    Map<String, Event> typeEvents = dataSnapshot.getValue(genericTypeIndicator);
                    processTypeEvents(type, typeEvents, task);
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private static void processTypeEvents(EventType type, Map<String, Event> typeEvents, Task<Event> task) {
        if(type == EventType.move) {
            LinkedList<Event> movements = new LinkedList<>(typeEvents.values());
            sort(movements);
            Event lastMove = movements.getLast();
            task.execute(lastMove);
            removeEvents(type);
        } else {
            for (Event event : typeEvents.values()) {
                task.execute(event);
                removeEvent(event);
            }
        }
    }

    private static void removeEvents(EventType type) {
        events().child(getProfile().getId()).child(type.name()).removeValue();
    }

    private static void removeEvent(Event event) {
        events().child(getProfile().getId()).child(event.getType().name())
                .child(event.getId()).removeValue();
    }


    // Noxboxes API
    private static DatabaseReference currentNoxboxReference() {
        return profiles().child(getProfile().getId()).child("profile").child("current");
    }

    public static void updateCurrentNoxbox(Noxbox noxbox) {
        if(noxbox == null) return;
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

    private static final String defaultName = "Unnamed person";

    // Profiles API
    public static void readProfile(final Task<Profile> task) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profiles().child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(UserAccount.class);
                    setDefaultValues();
                    refreshNotificationToken();

                    if((firebaseUser.getDisplayName() != null &&
                            !firebaseUser.getDisplayName().equals(getProfile().getName()))) {
                        getProfile().setName(firebaseUser.getDisplayName());
                        getProfile().getAcceptance().setCorrectNameProbability(1f);
                        updateProfile(getProfile());
                    }
                    if((firebaseUser.getPhotoUrl() != null && !firebaseUser.getPhotoUrl().toString().equals(getProfile().getPhoto()))) {
                        getProfile().setPhoto(firebaseUser.getPhotoUrl().toString());
                        getProfile().getAcceptance().setExpired(true);
                        updateProfile(getProfile());
                    }
                } else {
                    // create new profile
                    user = new UserAccount().setId(firebaseUser.getUid())
                            .setProfile(Profile.createFrom(firebaseUser))
                            .setRating(new AllRates())
                            .setWallet(new Wallet().setBalance("0"));
                    if(getProfile().getName() == null) {
                        getProfile().setName(defaultName);
                    }
                    updateProfile(getProfile());
                }
                task.execute(getProfile());
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private static void setDefaultValues() {
        if(getWallet() == null) {
            user.setWallet(new Wallet().setBalance("0"));
        }
        if(getRating() == null) {
            user.setRating(new AllRates());
        }
        if(getProfile().getAcceptance() == null) {
            getProfile().setAcceptance(new Acceptance(getProfile()));
        }
        if(getProfile().getName() == null) {
            getProfile().setName(defaultName);
        }
        if(user.getNotificationKeys() == null) {
            user.setNotificationKeys(new NotificationKeys());
        }
    }

    public static void refreshNotificationToken() {
        String notificationToken = FirebaseInstanceId.getInstance().getToken();
        if(notificationToken == null) return;
        if(!notificationToken.equals(user.getNotificationKeys().getAndroid())) {
            profiles().child(getProfile().getId()).child("notificationKeys")
                    .child("android").setValue(notificationToken);
        }
    }

    public static Profile getProfile() {
        return user == null ? null : user.getProfile();
    }

    public static Noxbox getCurrentNoxbox() {
        return getProfile() == null ? null : getProfile().getCurrent();
    }

    public static Wallet getWallet() {
        return user == null ? new Wallet() : user.getWallet();
    }

    public static void updateWallet(Wallet wallet) {
        if(user != null) {
            getWallet().setBalance(wallet.getBalance());
            getWallet().setAddress(wallet.getAddress());
            getWallet().setFrozenMoney(wallet.getFrozenMoney());
        }
    }

    public static AllRates getRating() {
        return user == null ? new AllRates() : user.getRating();
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
        getRating().getReceived().setLikes(getRating().getReceived().getLikes() + 1);
        getRating().getSent().setLikes(getRating().getSent().getLikes() + 1);
    }
}
