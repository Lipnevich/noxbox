package live.noxbox.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import live.noxbox.model.EventType;
import live.noxbox.model.Notice;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;

public class FirestoreReference {

    public static void createImageReference(final Activity activity, final Uri url, final ImageView image, final String childFolder) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), url);
            ImageManager.createCircleImageFromBitmap(activity, bitmap, image);

            final StorageReference storageRef =
                    com.google.firebase.storage.FirebaseStorage.getInstance()
                            .getReference()
                            .child(childFolder)
                            .child(FirebaseAuth.getInstance()
                                    .getCurrentUser()
                                    .getUid() + ".jpg");
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
                private final long time = System.currentTimeMillis();

                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());

                    Notice notice = new Notice().setType(EventType.uploadingProgress).setProgress(progress);

                    if (progress != 0) {
                        int passedSeconds = (int) (System.currentTimeMillis() - time) / 1000;
                        int secPerPercent = passedSeconds / progress;
                        int remainSeconds = (secPerPercent * (100 - progress));
                        notice.setTime(remainSeconds);
                    }

                    MessagingService messagingService = new MessagingService(activity.getApplicationContext());
                    messagingService.showPushNotification(notice);
                }
            });

        } catch (IOException e) {
            Crashlytics.logException(e);
        }
    }

    public static void createImageReference(final Activity activity, final Uri url, final String childFolder, final NoxboxType type) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), url);

            final StorageReference storageRef =
                    com.google.firebase.storage.FirebaseStorage.getInstance()
                            .getReference()
                            .child(childFolder)
                            .child(FirebaseAuth.getInstance()
                                    .getCurrentUser()
                                    .getUid() + ".jpg");
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

                                    if (childFolder.equals("certificates")) {
                                        profile.getPortfolio().get(type.name()).getCertificates().add(uri.toString());
                                    }

                                    if (childFolder.equals("workSamples")) {
                                        profile.getPortfolio().get(type.name()).getWorkSamples().add(uri.toString());
                                    }

                                    ProfileStorage.fireProfile();
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
                private final long time = System.currentTimeMillis();

                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());

                    Notice notice = new Notice().setType(EventType.uploadingProgress).setProgress(progress);

                    if (progress != 0) {
                        int passedSeconds = (int) (System.currentTimeMillis() - time) / 1000;
                        int secPerPercent = passedSeconds / progress;
                        int remainSeconds = (secPerPercent * (100 - progress));
                        notice.setTime(remainSeconds);
                    }

                    MessagingService messagingService = new MessagingService(activity.getApplicationContext());
                    messagingService.showPushNotification(notice);
                }
            });

        } catch (IOException e) {
            Crashlytics.logException(e);
        }
    }
}
