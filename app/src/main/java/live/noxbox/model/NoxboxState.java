package live.noxbox.model;

import static live.noxbox.model.Noxbox.isNullOrZero;

public enum NoxboxState {

    initial,
    created,
    requesting,
    accepting,
    moving,
    performing,
    completed;

    public static NoxboxState getState(Noxbox noxbox, Profile profile) {
        if (noxbox == null || isNullOrZero(noxbox.getTimeCreated()) || !isNullOrZero(noxbox.getTimeRemoved())
                || !isNullOrZero(noxbox.getTimeCompleted()) || !isNullOrZero(noxbox.getTimeCanceledByOwner())
                || !isNullOrZero(noxbox.getTimeCanceledByParty()) || !isNullOrZero(noxbox.getTimeTimeout()))
            return initial;


        if (!isNullOrZero(noxbox.getTimeCreated())
                && isNullOrZero(noxbox.getTimeRequested())
                && isNullOrZero(noxbox.getTimeAccepted())) {
            return created;
        }

        if (!isNullOrZero(noxbox.getTimeRequested())
                && isNullOrZero(noxbox.getTimeAccepted())
                && isNullOrZero(noxbox.getTimeCanceledByParty())
                && isNullOrZero(noxbox.getTimeCanceledByOwner())) {
            if (profile.equals(noxbox.getOwner())) {
                return accepting;
            }
            return requesting;
        }
        if (!isNullOrZero(noxbox.getTimeAccepted())
                && !isNullOrZero(noxbox.getTimeRequested())
                && (isNullOrZero(noxbox.getTimeOwnerVerified()) || isNullOrZero(noxbox.getTimePartyVerified()))) {
            return moving;
        }

        if (!isNullOrZero(noxbox.getTimePartyVerified()) &&
                !isNullOrZero(noxbox.getTimeOwnerVerified()) &&
                isNullOrZero(noxbox.getTimeCompleted())) {

            return performing;
        }
        return initial;
    }
}
