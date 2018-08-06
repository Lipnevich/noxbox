package live.noxbox.model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

public class Acceptance {

    private Boolean expired = true;
    private Float correctNameProbability = 0f;
    private Boolean failToRecognizeFace = true;
    private Float smileProbability = 0f;
    private Float rightEyeOpenProbability = 0f;
    private Float leftEyeOpenProbability = 0f;

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
    public Boolean isAccepted(float minProbability) {
        return correctNameProbability > minProbability
                && !failToRecognizeFace
                && !expired
                && smileProbability > minProbability
                && rightEyeOpenProbability > minProbability
                && leftEyeOpenProbability > minProbability;
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

    public Boolean getExpired() {
        return expired;
    }

    public Acceptance setExpired(Boolean expired) {
        this.expired = expired;
        return this;
    }
}
