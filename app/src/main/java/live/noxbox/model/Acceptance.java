package live.noxbox.model;

import com.google.firebase.database.Exclude;

import live.noxbox.tools.InvalidAcceptance;

import static live.noxbox.Configuration.MINIMUM_FACE_SIZE;
import static live.noxbox.Configuration.MINIMUM_PROBABILITY_FOR_ACCEPTANCE;

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
    public InvalidAcceptance getInvalidAcceptance() {
        if (faceSize < MINIMUM_FACE_SIZE) return new InvalidAcceptance.FaceSize();

        if (smileProbability < MINIMUM_PROBABILITY_FOR_ACCEPTANCE)
            return new InvalidAcceptance.Smile();

        if (rightEyeOpenProbability < MINIMUM_PROBABILITY_FOR_ACCEPTANCE || leftEyeOpenProbability < MINIMUM_PROBABILITY_FOR_ACCEPTANCE)
            return new InvalidAcceptance.Eyes();

        if (failToRecognizeFace) return new InvalidAcceptance.Face();

        return new InvalidAcceptance.None();
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
