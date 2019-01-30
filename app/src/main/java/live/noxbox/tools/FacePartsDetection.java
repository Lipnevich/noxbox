package live.noxbox.tools;

import android.app.Activity;
import android.graphics.Bitmap;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.HashMap;
import java.util.Map;

import live.noxbox.debug.TimeLogger;
import live.noxbox.model.Acceptance;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;

import static live.noxbox.Constants.MINIMUM_FACE_SIZE;

public class FacePartsDetection {

    public static void execute(final Bitmap bitmap, final Profile profile,
                               final Activity activity, final Task<Bitmap> task) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.FAST_MODE)
                        .setLandmarkType(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(MINIMUM_FACE_SIZE)
                        .build();
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
        final TimeLogger detection = new TimeLogger();
        detector.detectInImage(image)
                .addOnSuccessListener(faces -> {
                    detection.makeLog("detection");

                    Map<String, String> data = new HashMap<>();

                    if (faces.size() != 1) {
                        profile.setAcceptance(new Acceptance());
                    } else {
                        profile.getAcceptance().setFailToRecognizeFace(false);
                        profile.getAcceptance().setFaceSize(0.7f);

                        FirebaseVisionFace face = faces.get(0);
                        // If classification was enabled:
                        profile.getAcceptance().setSmileProbability(face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY ? face.getSmilingProbability() : 0f);

                        profile.getAcceptance().setRightEyeOpenProbability(face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY ? face.getRightEyeOpenProbability() : 0f);

                        profile.getAcceptance().setLeftEyeOpenProbability(face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY ? face.getLeftEyeOpenProbability() : 0f);
                    }

                    if (profile.getAcceptance().isAccepted()) {
                        data.put("type", NotificationType.photoValid.name());
                        NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data).show();
                        task.execute(bitmap);

                    } else {
                        data.put("type", NotificationType.photoInvalid.name());
                        NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data).show();
                        LogEvents.generateLogEvent(activity, "invalid_profile_photo");
                    }

                    //AppCache.fireProfile();
                    ProgressDialogManager.hideProgress();
                })
                .addOnFailureListener(e -> {
                    Crashlytics.logException(e);
                    profile.getAcceptance().setFailToRecognizeFace(true);

                    Map<String, String> data = new HashMap<>();
                    data.put("type", NotificationType.photoInvalid.name());
                    NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data).show();

                    //AppCache.fireProfile();

                    ProgressDialogManager.hideProgress();
                });
    }

}