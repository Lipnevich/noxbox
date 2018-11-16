package live.noxbox.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;

import live.noxbox.database.AppCache;
import live.noxbox.model.NotificationData;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.util.MessagingService;

import static live.noxbox.Configuration.MINIMUM_FACE_SIZE;

public class FacePartsDetection {


    public static void execute(Bitmap bitmap, final Profile profile, final Activity activity) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                        .setLandmarkType(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(MINIMUM_FACE_SIZE)
                        .setTrackingEnabled(false)
                        .build();
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> faces) {
                        if (faces.size() != 1) {
                            profile.getAcceptance().setFailToRecognizeFace(true);
                            buildNotification(new NotificationData().setType(NotificationType.photoInvalid).setInvalidAccetrance(profile.getAcceptance().getInvalidAcceptance()), activity);
                            AppCache.fireProfile();
                            return;
                        }
                        profile.getAcceptance().setFailToRecognizeFace(false);
                        FirebaseVisionFace face = faces.get(0);
                        FirebaseVisionFaceLandmark nose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                        if (nose != null) {
                            profile.getAcceptance().setNose(true);
                        }

                        // If classification was enabled:
                        if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            profile.getAcceptance().setSmileProbability(face.getSmilingProbability());
                        }
                        if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            profile.getAcceptance().setRightEyeOpenProbability(face.getRightEyeOpenProbability());
                        }
                        if (face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            profile.getAcceptance().setLeftEyeOpenProbability(face.getLeftEyeOpenProbability());
                        }

                        if (profile.getAcceptance().isAccepted()) {
                            buildNotification(new NotificationData().setType(NotificationType.photoValid), activity);
                        } else {
                            profile.getAcceptance().setFailToRecognizeFace(true);
                            buildNotification(new NotificationData().setType(NotificationType.photoInvalid).setInvalidAccetrance(profile.getAcceptance().getInvalidAcceptance()), activity);
                        }

                        AppCache.fireProfile();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        profile.getAcceptance().setFailToRecognizeFace(true);
                        buildNotification(new NotificationData().setType(NotificationType.photoInvalid).setInvalidAccetrance(profile.getAcceptance().getInvalidAcceptance()), activity);
                        AppCache.fireProfile();
                    }
                });
    }

    private static void buildNotification(NotificationData notification, Activity activity) {
        MessagingService messagingService = new MessagingService(activity.getApplicationContext());
        messagingService.showPushNotification(notification);
    }
}