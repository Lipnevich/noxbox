package live.noxbox.model;

import java.util.HashMap;
import java.util.Map;

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
    verifyPhoto(2, R.string.pleaseConfirmMember),
    performing(2, R.string.performingPushContent),
    lowBalance(2, R.string.beforeSpendingMoney),
    completed(2, R.string.completedPushContent),
    canceled(2, R.string.serviceWasCanceled),
    rejected(2, R.string.serviceWasCanceled),

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

    public static Map<String, String> fromNoxboxState(Profile profile) {
        Noxbox current  = profile.getCurrent();
        NoxboxState state = NoxboxState.getState(current, profile);
        Map<String, String> data = new HashMap<>();
        switch (state) {
            case requesting: {
                data.put("type", NotificationType.requesting.name());
                data.put("time", current.getTimeRequested() + "");
            }

            case accepting: {
                data.put("type", NotificationType.accepting.name());
                data.put("id", current.getId());
            }
            case moving: {
                data.put("type", NotificationType.moving.name());
                data.put("id", current.getId());
            }
            case performing: {
                data.put("type", NotificationType.performing.name());
                data.put("id", current.getId());
            }

        }
        return data;
    }
}
