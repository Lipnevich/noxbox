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

import java.util.List;

import live.noxbox.database.AppCache;
import live.noxbox.debug.TimeLogger;
import live.noxbox.model.NotificationData;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.util.MessagingService;

import static live.noxbox.Configuration.MINIMUM_FACE_SIZE;

public class FacePartsDetection {

//    private static float getFaceSizeFromBitmap(Bitmap bitmap, Activity activity) {
//        FaceDetector detector = new FaceDetector.Builder(activity)
//                .setTrackingEnabled(false)
//                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
//                .build();
//        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
//        SparseArray<Face> faces = detector.detect(frame);
//
//        if (faces.size() == 0) return 0f;
//
//        Face face = faces.valueAt(0);
//
//        float faceArea = face.getWidth() * face.getHeight();
//        float bitmapArea = bitmap.getWidth() * bitmap.getHeight();
//
//        return faceArea / bitmapArea;
//    }

    public static void execute(final Bitmap bitmap, final Profile profile, final Activity activity) {
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
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> faces) {
                        detection.makeLog("detection");
                        if (faces.size() != 1) {
                            profile.getAcceptance().setFailToRecognizeFace(true);
                            buildNotification(new NotificationData().setType(NotificationType.photoInvalid).setInvalidAccetrance(profile.getAcceptance().getInvalidAcceptance()), activity);
                            AppCache.fireProfile();
                            return;
                        }
                        profile.getAcceptance().setFailToRecognizeFace(false);

                        profile.getAcceptance().setFaceSize(0.7f);
                        FirebaseVisionFace face = faces.get(0);


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