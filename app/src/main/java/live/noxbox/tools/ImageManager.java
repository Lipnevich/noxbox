package live.noxbox.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import live.noxbox.database.AppCache;
import live.noxbox.debug.TimeLogger;
import live.noxbox.model.ImageType;
import live.noxbox.model.NotificationData;
import live.noxbox.model.NotificationType;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.util.MessagingService;

public class ImageManager {


    public static void uploadPhoto(final Activity activity, final Profile profile, Bitmap bitmap) {
        uploadImage(activity, bitmap, "photos/profile", new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                MessagingService messagingService = new MessagingService(activity.getApplicationContext());
                messagingService.showPushNotification(new NotificationData().setType(NotificationType.photoValid));
                profile.setPhoto(uri.toString());
            }
        });
    }

    public static void uploadImage(final Activity activity, final Uri url, final ImageType imageType, final NoxboxType type, final int index) {
        getBitmap(activity, url, new Task<Bitmap>() {
            @Override
            public void execute(Bitmap bitmap) {
                if (bitmap == null) return;
                uploadImage(activity, bitmap, type.name() + "/" + imageType.name() + "/" + index, new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        AppCache.readProfile(new Task<Profile>() {
                            @Override
                            public void execute(Profile profile) {
                                profile.getPortfolio().get(type.name()).getImages().get(imageType.name()).add(uri.toString());
                                AppCache.fireProfile();
                            }
                        });
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

        int quality = 100;
        // TODO (nli) обрезать фото по опознанному лицу до ужатия
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        while (quality > 1) {
            quality -= 5;
            if (stream.toByteArray().length <= 800000)//check photo size <= 800B or compress to lower quality
                break;
            stream = new ByteArrayOutputStream();
            TimeLogger compression = new TimeLogger();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            compression.makeLog("compression");
        }

        UploadTask uploadTask = storageRef.putBytes(stream.toByteArray());
        final MessagingService messagingService = new MessagingService(activity.getApplicationContext());
        final NotificationData notification = new NotificationData();
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
//                    NotificationType.updateNotification(activity.getApplicationContext(), notification, MessagingService.builder);
                }
            }
        });
    }

    public static void getBitmap(Activity activity, Uri url, final Task<Bitmap> task) {
        Glide.with(activity).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                task.execute(bitmap);
            }
        });
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
