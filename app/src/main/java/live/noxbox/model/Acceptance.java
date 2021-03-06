package live.noxbox.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import static live.noxbox.Constants.MINIMUM_FACE_SIZE;
import static live.noxbox.Constants.MINIMUM_PROBABILITY_FOR_ACCEPTANCE;

public class Acceptance {

    private Boolean failToRecognizeFace = true;
    private Boolean incorrectName = false;
    private Float smileProbability = 0f;
    private Float rightEyeOpenProbability = 0f;
    private Float leftEyeOpenProbability = 0f;
    private Float faceSize = 0f;
    private String message = "";

    public Acceptance() {
    }

    @NonNull
    @Override
    public String toString() {
        return "failToRecognizeFace: " + failToRecognizeFace + ", "
                + "incorrectName: " + incorrectName + ", "
                + "smileProbability: " + smileProbability + ", "
                + "rightEyeOpenProbability: " + rightEyeOpenProbability + ", "
                + "leftEyeOpenProbability: " + leftEyeOpenProbability + ", "
                + "faceSize: " + faceSize + ", "
                + "message: " + message;
    }

    @Exclude
    public Boolean isAccepted() {
        if (failToRecognizeFace) return false;
//        if (incorrectName) return false;
        return faceSize >= MINIMUM_FACE_SIZE
                && smileProbability > MINIMUM_PROBABILITY_FOR_ACCEPTANCE
                && rightEyeOpenProbability > MINIMUM_PROBABILITY_FOR_ACCEPTANCE
                && leftEyeOpenProbability > MINIMUM_PROBABILITY_FOR_ACCEPTANCE;
    }

    @Exclude
    public live.noxbox.model.InvalidAcceptance getInvalidAcceptance() {
        if (faceSize < MINIMUM_FACE_SIZE) return live.noxbox.model.InvalidAcceptance.faceSize;

        if (smileProbability < MINIMUM_PROBABILITY_FOR_ACCEPTANCE)
            return live.noxbox.model.InvalidAcceptance.smile;

        if (rightEyeOpenProbability < MINIMUM_PROBABILITY_FOR_ACCEPTANCE || leftEyeOpenProbability < MINIMUM_PROBABILITY_FOR_ACCEPTANCE)
            return live.noxbox.model.InvalidAcceptance.eyes;

        if (failToRecognizeFace) return live.noxbox.model.InvalidAcceptance.face;

        return live.noxbox.model.InvalidAcceptance.none;
    }

    public Float getSmileProbability() {
        return smileProbability;
    }

    public Acceptance setSmileProbability(Float smileProbability) {
        this.smileProbability = smileProbability;
        return this;
    }

    public Float getRightEyeOpenProbability() {
        return rightEyeOpenProbability;
    }

    public Acceptance setRightEyeOpenProbability(Float rightEyeOpenProbability) {
        this.rightEyeOpenProbability = rightEyeOpenProbability;
        return this;
    }

    public Float getLeftEyeOpenProbability() {
        return leftEyeOpenProbability;
    }

    public Acceptance setLeftEyeOpenProbability(Float leftEyeOpenProbability) {
        this.leftEyeOpenProbability = leftEyeOpenProbability;
        return this;
    }

    public Boolean getFailToRecognizeFace() {
        return failToRecognizeFace;
    }

    public Acceptance setFailToRecognizeFace(Boolean failToRecognizeFace) {
        this.failToRecognizeFace = failToRecognizeFace;
        return this;
    }

    public Boolean getIncorrectName() {
        return incorrectName;
    }

    public Acceptance setIncorrectName(Boolean incorrectName) {
        this.incorrectName = incorrectName;
        return this;
    }

    public Float getFaceSize() {
        return faceSize;
    }

    public Acceptance setFaceSize(Float faceSize) {
        this.faceSize = faceSize;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Acceptance setMessage(String message) {
        this.message = message;
        return this;
    }


}
