package live.noxbox.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import live.noxbox.R;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.analitics.BusinessEvent;
import live.noxbox.database.AppCache;
import live.noxbox.model.ImageType;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.Notification;

import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.notifications.factory.NotificationFactory.buildNotification;

public class ImageManager {

    public static void uploadPhoto(final Activity activity, final Profile profile, Bitmap bitmap) {
        uploadImage(activity, bitmap, "profile/" + System.currentTimeMillis(), uri -> {
            Map<String, String> data = new HashMap<>();
            data.put("type", NotificationType.photoValid.name());
            buildNotification(activity.getApplicationContext(), null, data).show();
            BusinessActivity.businessEvent(BusinessEvent.validPhoto);
            profile.setPhoto(uri.toString());

            if (profile.getCurrent() != null
                    && profile.getNoxboxId() != null
                    && !isNullOrZero(profile.getCurrent().getTimeCreated())
                    && isNullOrZero(profile.getCurrent().getTimePartyRejected())
                    && isNullOrZero(profile.getCurrent().getTimeOwnerRejected())) {
                Noxbox current = profile.getCurrent();
                if (current.getOwner().equals(profile) && isNullOrZero(current.getTimePartyVerified())) {
                    current.getOwner().setPhoto(profile.getPhoto());
                    AppCache.updateNoxbox();
                } else if (current.getParty().equals(profile) && isNullOrZero(current.getTimeOwnerVerified())) {
                    current.getParty().setPhoto(profile.getPhoto());
                    AppCache.updateNoxbox();
                }

            }

            AppCache.fireProfile();
        });
    }

    public static void uploadImage(final Activity activity, final Uri url, final ImageType imageType, final NoxboxType type, final int index) {
        getBitmap(activity, url, bitmap -> {
            if (bitmap == null) return;
            uploadImage(activity, bitmap, type.name() + "/" + imageType.name() + "/" + index,
                    uri -> {
                        AppCache.profile().getPortfolio().get(type.name()).getImages().get(imageType.name()).add(uri.toString());
                        AppCache.fireProfile();
                    });
        });

    }

    private static final OnFailureListener onFailureListener = e -> Crashlytics.logException(e);

    private static void uploadImage(final Activity activity, final Bitmap bitmap, final String path, final OnSuccessListener<Uri> onSuccessListener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        final StorageReference storageRef =
                getStorageReference(user.getUid()).child(path + "." + Bitmap.CompressFormat.JPEG.name());

        int quality = 100;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        while (quality > 5) {
            quality -= 5;
            if (stream.toByteArray().length <= 800000)//check photo size <= 800B or compress to lower quality
                break;
            stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        }

        final Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.photoUploadingProgress.name());
        data.put("progress", String.valueOf(0));
        final Notification notificationUploadingProgress = buildNotification(activity.getApplicationContext(), null, data);
        notificationUploadingProgress.show();
        UploadTask uploadTask = storageRef.putBytes(stream.toByteArray());
        uploadTask
                .addOnFailureListener(onFailureListener)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(onSuccessListener)
                        .addOnFailureListener(onFailureListener))
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    private final long time = System.currentTimeMillis();

                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());


                        if (progress != 0) {
                            int passedSeconds = (int) (System.currentTimeMillis() - time) / 1000;
                            int secPerPercent = passedSeconds / progress;
                            int remainSeconds = (secPerPercent * (100 - progress));
                        }
                        notificationUploadingProgress.update(Collections.singletonMap("progress", String.valueOf(progress)));

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

    public static void getBitmap(Activity activity, Bitmap bitmap, final Task<Bitmap> task) {
        Glide.with(activity).asBitmap().load(bitmap).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                task.execute(bitmap);
            }
        });
    }

    private static StorageReference getStorageReference(String userId) {
        return FirebaseStorage.getInstance().getReference().child("images").child(userId);
    }

    public static void deleteImage(final NoxboxType type, final int index, final ImageType imageType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        getStorageReference(user.getUid())
                .child(type.name() + "/" + imageType.name() + "/" + index + "." + Bitmap.CompressFormat.JPEG.name())
                .delete()
                .addOnFailureListener(onFailureListener);
    }

    public static void deleteFolderByType(final NoxboxType type) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        getStorageReference(user.getUid()).child(type.name()).delete().addOnFailureListener(onFailureListener);
    }


    public static void createCircleProfilePhotoFromUrl(Activity activity, String url, ImageView image) {
        if (activity.isFinishing()) return;
        if (url != null)
            Glide.with(activity)
                    .asDrawable()
                    .load(url)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                    .apply(RequestOptions.circleCropTransform())
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable drawable, @Nullable Transition<? super Drawable> transition) {
                            if (!activity.isFinishing()) {
                                image.setImageDrawable(drawable);
                            }
                        }
                    });
        else
            createPlaceholderForProfilePhoto(activity, image);
    }

    public static void createCircleImageFromBitmap(Activity activity, Bitmap bitmap, ImageView image) {
        if (activity.isFinishing()) return;
        Glide.with(activity)
                .asDrawable()
                .load(bitmap)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .apply(RequestOptions.circleCropTransform())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable drawable, @Nullable Transition<? super Drawable> transition) {
                        if (!activity.isFinishing()) {
                            image.setImageDrawable(drawable);
                        }
                    }
                });
    }

    private static void createPlaceholderForProfilePhoto(Activity activity, ImageView image) {
        if (activity.isFinishing()) return;
        Glide.with(activity)
                .asDrawable()
                .load(R.drawable.human_profile)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .apply(RequestOptions.circleCropTransform())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable drawable, @Nullable Transition<? super Drawable> transition) {
                        if (!activity.isFinishing()) {
                            image.setImageDrawable(drawable);
                        }
                    }
                });
    }

}
