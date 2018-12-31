package live.noxbox.model;

import live.noxbox.R;

/**
 * Created by nicolay.lipnevich on 13/05/2017.
 */
public enum NotificationType {

    photoValidationProgress(0, R.string.photoValidationProgressContent),
    photoUploadingProgress(0, R.string.uploadingProgressTitle),
    photoValid(0, R.string.photoValidContent),
    photoInvalid(0, R.string.photoInvalidContent),

    balance(1, R.string.balancePushContent),

    requesting(2, R.string.requestingPushContent),
    accepting(2, R.string.acceptingPushContent),
    moving(2, R.string.replaceIt),
    verifyPhoto(2, R.string.replaceIt),
    performing(2, R.string.performingPushContent),
    lowBalance(2, R.string.beforeSpendingMoney),
    completed(2, R.string.completedPushContent),
    canceled(2, R.string.serviceWasCanceled),

    refund(3, R.string.replaceIt),

    message(4, R.string.replaceIt),

    support(5, R.string.replaceIt);


    private int group;
    private int content;

    NotificationType(int id, int content) {
        this.group = id;
        this.content = content;
    }

    public int getGroup() {
        return group;
    }


    public int getContent() {
        return content;
    }

}
