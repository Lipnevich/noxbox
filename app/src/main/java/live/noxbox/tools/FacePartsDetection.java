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

import live.noxbox.model.Acceptance;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;

import static live.noxbox.Constants.MINIMUM_FACE_SIZE;
import static live.noxbox.analitics.BusinessActivity.businessEvent;
import static live.noxbox.analitics.BusinessEvent.invalidPhoto;

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
        detector.detectInImage(image)
                .addOnSuccessListener(faces -> {
                    Acceptance acceptance = new Acceptance();
                    acceptance.setIncorrectName(profile.getAcceptance().getIncorrectName());
                    acceptance.setMessage(profile.getAcceptance().getMessage());

                    if (faces.size() == 1) {
                        acceptance.setFailToRecognizeFace(false);
                        acceptance.setFaceSize(0.7f);

                        FirebaseVisionFace face = faces.get(0);
                        // If classification was enabled:
                        acceptance.setSmileProbability(face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY ? face.getSmilingProbability() : 0f);

                        acceptance.setRightEyeOpenProbability(face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY ? face.getRightEyeOpenProbability() : 0f);

                        acceptance.setLeftEyeOpenProbability(face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY ? face.getLeftEyeOpenProbability() : 0f);
                    }
                    if (acceptance.isAccepted()) {
                        task.execute(bitmap);
                    } else {
                        Map<String, String> data = new HashMap<>();
                        data.put("type", NotificationType.photoInvalid.name());
                        NotificationFactory.buildNotification(activity.getApplicationContext(), new Profile().setAcceptance(acceptance), data).show();
                        businessEvent(invalidPhoto);
                    }

                })
                .addOnFailureListener(e -> {
                    Crashlytics.logException(e);
                    profile.getAcceptance().setFailToRecognizeFace(true);

                    Map<String, String> data = new HashMap<>();
                    data.put("type", NotificationType.photoInvalid.name());
                    NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data).show();
                });
    }

}