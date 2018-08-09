package live.noxbox.state;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import javax.annotation.Nullable;

import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

public class Firestore {

    static {
        FirebaseFirestore.getInstance()
                .setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build());
    }


    private static FirebaseFirestore db() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build());

        return db;
    }


    // Profiles API
    static void listenProfile(@NonNull final Task<Profile> task) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference docRef = db().collection("profiles").document(firebaseUser.getUid());

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

            }
        });


//        profiles().child(firebaseUser.getUid()).child("profile").addValueEventListener(new ValueEventListener() {
//            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    profile = snapshot.getValue(Profile.class);
//                    refreshNotificationToken();
//                    task.execute(profile);
//                }
//            }
//            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
//        });

    }
}
