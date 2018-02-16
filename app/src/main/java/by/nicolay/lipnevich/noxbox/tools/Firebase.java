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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.model.AllRates;
import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.model.UserAccount;
import by.nicolay.lipnevich.noxbox.model.UserType;
import by.nicolay.lipnevich.noxbox.model.Wallet;

import static java.lang.reflect.Modifier.isStatic;

public class Firebase {

    private static DatabaseReference profiles;
    private static DatabaseReference messages;
    private static DatabaseReference requests;
    private static GeoFire availablePerformers;
    private static UserAccount userAccount;
    private static NoxboxType type;
    private static UserType userType;

    public static void init(NoxboxType noxboxType, UserType userOfType) {
        type = noxboxType;
        userType = userOfType;
        profiles = FirebaseDatabase.getInstance().getReference().child("profiles");
        requests = FirebaseDatabase.getInstance().getReference().child("requests");
        messages = FirebaseDatabase.getInstance().getReference().child("messages");
        availablePerformers = new GeoFire(FirebaseDatabase.getInstance().getReference().child("availablePerformers").child(type.toString()));
    }

    public static BigDecimal getPrice() {
        // TODO (nli) get it from db
        return new BigDecimal(0.001).setScale(5, RoundingMode.UP);
    }

    public static void sendRequest(Request request) {
        requests.child(getProfile().getId()).child(request.getType().toString())
                .setValue(objectToMap(request.setId(getProfile().getId()).setNoxboxType(type)));
    }

    // Messages API
    public static Message sendMessage(String profileId, Message message) {
        String messageKey = messages.child(profileId).push().getKey();
        messages.child(profileId).child(messageKey).updateChildren(objectToMap(message
                .setId(messageKey).setSender(getProfile().publicInfo()).setTime(System.currentTimeMillis())));
        return message;
    }

    public static void sendMessageForEveryoneExceptMe(Message message) {
        Noxbox noxbox = currentNoxboxes().get(type.toString());
        for(Profile payer : noxbox.getPayers().values()) {
            if(!payer.getId().equals(getProfile().getId())) {
                sendMessage(payer.getId(), message);
            }
        }
        for(Profile performer : noxbox.getPerformers().values()) {
            if(!performer.getId().equals(getProfile().getId())) {
                sendMessage(performer.getId(), message);
            }
        }
    }

    public static void addPayerMessage(String payerId, String message) {
        Noxbox noxbox = currentNoxboxes().get(type.toString());
        noxbox.getPayers().get(payerId).getMessages().add(message);
        updateCurrentNoxbox(noxbox);
    }

    public static void addPerformerMessage(String performerId, String message) {
        Noxbox noxbox = currentNoxboxes().get(type.toString());
        noxbox.getPerformers().get(performerId).getMessages().add(message);
        updateCurrentNoxbox(noxbox);
    }

    public static void listenMessages(final Task task) {
        messages.child(getProfile().getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, Message>> genericTypeIndicator
                            = new GenericTypeIndicator<Map<String, Message>>() {};
                    Map<String, Message> allMessages = dataSnapshot.getValue(genericTypeIndicator);
                    for(Message message : allMessages.values()) {
                        task.execute(message);
                    }
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public static void removeMessage(String messageKey) {
        messages.child(getProfile().getId()).child(messageKey).removeValue();
    }

    // Noxboxes API
    private static DatabaseReference currentNoxboxReference() {
        if(userType.equals(UserType.payer)) {
            return profiles.child(getProfile().getId()).child("profile").child("noxboxesForPayer").child(type.toString());
        } else if (userType.equals(UserType.performer)) {
            return profiles.child(getProfile().getId()).child("profile").child("noxboxesForPerformer").child(type.toString());
        }
        throw new IllegalArgumentException("Unknown profile type");
    }

    private static Map<String, Noxbox> currentNoxboxes() {
        if(userType.equals(UserType.payer)) {
            return getProfile().getNoxboxesForPayer();
        } else if (userType.equals(UserType.performer)) {
            return getProfile().getNoxboxesForPerformer();
        }
        throw new IllegalArgumentException("Unknown profile type");
    }

    public static void updateCurrentNoxbox(Noxbox noxbox) {
        currentNoxboxReference().updateChildren(objectToMap(noxbox));
        currentNoxboxes().put(type.toString(), noxbox);
    }

    public static void removeCurrentNoxbox() {
        currentNoxboxReference().removeValue();
        currentNoxboxes().remove(type.toString());
    }

    // History API
    public static Noxbox createHistory(Noxbox noxbox) {
        getHistoryReference().child(noxbox.getId()).updateChildren(objectToMap(noxbox));
        return noxbox;
    }

    public static void loadHistory(final Task task) {
        getHistoryReference().limitToLast(15).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private static DatabaseReference getHistoryReference() {
        return profiles.child(getProfile().getId()).child(userType.name() + "History").child(type.toString());
    }

    private static final String defaultName = "Unnamed person";

    // Profiles API
    public static void readProfile(final Task task) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        profiles.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userAccount = snapshot.getValue(UserAccount.class);
                    if(getWallet() == null) {
                        userAccount.setWallet(new Wallet().setBalance("0"));
                    }
                    if(getRating() == null) {
                        userAccount.setRating(new AllRates());
                    }
                    if(getProfile().getName() == null) {
                        getProfile().setName(defaultName);
                    }
                    if((user.getDisplayName() != null && !user.getDisplayName().equals(getProfile().getName())) ||
                            (user.getPhotoUrl() != null && !user.getPhotoUrl().toString().equals(getProfile().getPhoto()))) {
                        getProfile().setName(user.getDisplayName());
                        getProfile().setPhoto(user.getPhotoUrl().toString());
                        updateProfile(getProfile());
                    }
                } else {
                    // create new profile
                    userAccount = new UserAccount().setId(user.getUid())
                            .setProfile(Profile.createFrom(user))
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

    public static Profile getProfile() {
        return userAccount == null ? null : userAccount.getProfile();
    }

    public static Wallet getWallet() {
        return userAccount == null ? new Wallet() : userAccount.getWallet();
    }

    public static AllRates getRating() {
        return userAccount == null ? new AllRates() : userAccount.getRating();
    }

    public static void updateProfile(Profile profile) {
        profiles.child(profile.getId()).child("profile").updateChildren(objectToMap(profile));
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

    public static Noxbox tryGetNoxboxInProgress() {
        if(getProfile() != null && currentNoxboxes() != null) {
            return currentNoxboxes().get(type.toString());
        }
        return null;
    }

    // TODO (nli) move all methods for it to current class
    public static GeoFire getAvailablePerformers() {
        return availablePerformers;
    }

    public static void like() {
        getRating().getReceived().setLikes(getRating().getReceived().getLikes() + 1);
        getRating().getSent().setLikes(getRating().getSent().getLikes() + 1);
    }
}
