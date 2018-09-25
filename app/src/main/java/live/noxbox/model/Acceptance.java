package live.noxbox.model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

import static live.noxbox.Configuration.MINIMUM_PROBABILITY_FOR_ACCEPTANCE;

public class Acceptance {

    private Float correctNameProbability = 0f;
    private Boolean failToRecognizeFace = true;
    private Float smileProbability = 0f;
    private Float rightEyeOpenProbability = 0f;
    private Float leftEyeOpenProbability = 0f;
    private Boolean isNose = false;

    public Acceptance() {

    }

    public Acceptance(FirebaseUser user) {
        correctNameProbability = user.getDisplayName() != null ? 1f : 0f;
        failToRecognizeFace = user.getPhotoUrl() != null ? false : true;
    }

    public Acceptance(Profile profile) {
        correctNameProbability = profile.getName() != null ? 1f : 0f;
        failToRecognizeFace = profile.getPhoto() != null ? false : true;
    }

    @Exclude
    public Boolean isAccepted() {
        return //correctNameProbability > MINIMUM_PROBABILITY_FOR_ACCEPTANCE &&
                !failToRecognizeFace
                && smileProbability > MINIMUM_PROBABILITY_FOR_ACCEPTANCE
                && rightEyeOpenProbability > MINIMUM_PROBABILITY_FOR_ACCEPTANCE
                && leftEyeOpenProbability > MINIMUM_PROBABILITY_FOR_ACCEPTANCE
                && isNose;
    }

    public Float getCorrectNameProbability() {
        return correctNameProbability;
    }

    public Acceptance setCorrectNameProbability(Float correctNameProbability) {
        this.correctNameProbability = correctNameProbability;
        return this;
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

    public Boolean getNose() {
        return isNose;
    }

    public Acceptance setNose(Boolean nose) {
        isNose = nose;
        return this;
    }
}
