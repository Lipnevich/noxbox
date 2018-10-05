package live.noxbox.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import live.noxbox.R;
import live.noxbox.model.ImageType;
import live.noxbox.model.Notification;
import live.noxbox.model.NotificationType;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;

public class ImageManager {


    public static void uploadPhoto(final Activity activity, final Uri uri) {
        final Bitmap bitmap = getBitmap(activity, uri);

        if (bitmap == null) return;

        createCircleImageFromBitmap(activity, bitmap, ((ImageView) activity.findViewById(R.id.profileImage)));

        uploadImage(activity, bitmap, "photos/profile", new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                ProfileStorage.readProfile(new Task<Profile>() {
                    @Override
                    public void execute(final Profile profile) {
                        MessagingService messagingService = new MessagingService(activity.getApplicationContext());
                        messagingService.showPushNotification(new Notification().setType(NotificationType.photoValidationProgress));
                        profile.setPhoto(uri.toString());
                        FacePartsDetection.execute(bitmap, profile, activity);


                    }
                });
            }
        });
    }

    public static void uploadImage(final Activity activity, final Uri url, final ImageType imageType, final NoxboxType type, final int index) {
        Bitmap bitmap = getBitmap(activity, url);

        if (bitmap == null) return;

        uploadImage(activity, bitmap, type.name() + "/" + imageType.name() + "/" + index, new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                ProfileStorage.readProfile(new Task<Profile>() {
                    @Override
                    public void execute(Profile profile) {
                        profile.getPortfolio().get(type.name()).getImages().get(imageType.name()).add(uri.toString());
                        ProfileStorage.fireProfile();
                    }
                });
            }
        });
    }

    private static final OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Crashlytics.logException(e);
        }
    };

    private static void uploadImage(final Activity activity, final Bitmap bitmap, final String path, final OnSuccessListener<Uri> onSuccessListener) {

        final StorageReference storageRef =
                getStorageReference().child(path + "." + Bitmap.CompressFormat.JPEG.name());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        //TODO (vl) check photo size <= 1MB or compress to lower quality
        UploadTask uploadTask = storageRef.putBytes(baos.toByteArray());
        final MessagingService messagingService = new MessagingService(activity.getApplicationContext());
        final Notification notification = new Notification();
        uploadTask
                .addOnFailureListener(onFailureListener)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(onSuccessListener)
                                .addOnFailureListener(onFailureListener);
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            private final long time = System.currentTimeMillis();

            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());

                notification.setType(NotificationType.uploadingProgress).setProgress(progress).setMaxProgress(100);

                if (progress != 0) {
                    int passedSeconds = (int) (System.currentTimeMillis() - time) / 1000;
                    int secPerPercent = passedSeconds / progress;
                    int remainSeconds = (secPerPercent * (100 - progress));
                    notification.setTime(String.valueOf(remainSeconds));
                }
                if (progress == 0) {
                    messagingService.showPushNotification(notification);
                } else {
                    notification.getType().updateNotification(activity.getApplicationContext(), notification, MessagingService.builder);
                }

            }
        });
    }


    public static Bitmap getBitmap(final Activity activity, final Uri url) {
        try {
            return MediaStore.Images.Media.getBitmap(activity.getContentResolver(), url);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }
        return null;
    }

    private static StorageReference getStorageReference() {
        return FirebaseStorage.getInstance().getReference().child("images").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    public static void deleteImage(final NoxboxType type, final int index, final ImageType imageType) {
        getStorageReference()
                .child(type.name() + "/" + imageType.name() + "/" + index + "." + Bitmap.CompressFormat.JPEG.name())
                .delete()
                .addOnFailureListener(onFailureListener);
    }

    public static void deleteFolderByType(final NoxboxType type) {
        getStorageReference().child(type.name()).delete().addOnFailureListener(onFailureListener);
    }


    public static void createCircleImageFromUrl(Activity activity, String url, ImageView image) {
        Glide.with(activity)
                .asDrawable()
                .load(url)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .apply(RequestOptions.circleCropTransform())
                .into(image);
    }

    public static void createCircleImageFromBitmap(Activity activity, Bitmap bitmap, ImageView image) {
        Glide.with(activity)
                .asDrawable()
                .load(bitmap)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .apply(RequestOptions.circleCropTransform())
                .into(image);
    }
}
