package live.noxbox.tools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;

public class FirestoreReference {

    public static void createImageReference(final Activity activity, final Uri url, final ImageView image) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), url);
            ImageManager.createCircleImageFromBitmap(activity, bitmap, image);

            final StorageReference storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReference().child("photos").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //TODO (vl) check photo size <= 1MB or compress to lower quality
            UploadTask uploadTask = storageRef.putBytes(baos.toByteArray());
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Crashlytics.logException(e);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            ProfileStorage.readProfile(new Task<Profile>() {
                                @Override
                                public void execute(final Profile profile) {
                                    profile.setPhoto(uri.toString());
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Crashlytics.logException(e);
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    DialogManager.showProgress(progressDialog);
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if (progress == 100.0){
                        DialogManager.hideProgress(progressDialog);
                    }
                    Log.d(activity.getPackageName(), "Upload is " + progress + "% done");

                }

            });


        } catch (IOException e) {
            Crashlytics.logException(e);
        }
    }
}
