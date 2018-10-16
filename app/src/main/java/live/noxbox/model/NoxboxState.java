package live.noxbox.model;

public enum NoxboxState {

    initial,
    created,
    requesting,
    accepting,
    moving,
    performing,
    completed;

    public static NoxboxState getState(Noxbox noxbox, Profile profile) {
        if (noxbox == null || noxbox.getTimeCreated() == null || noxbox.getTimeRemoved() != null) return initial;


        if (noxbox.getTimeCreated() != null
                && noxbox.getTimeRequested() == null
                && noxbox.getTimeAccepted() == null) {
            return created;
        }

        if (noxbox.getTimeRequested() != null
                && noxbox.getTimeAccepted() == null
                && noxbox.getTimeCanceledByParty() == null
                && noxbox.getTimeCanceledByOwner() == null) {
            if (profile.equals(noxbox.getOwner())) {
                return accepting;
            }
            return requesting;
        }
        if (noxbox.getTimeAccepted() != null
                && noxbox.getTimeRequested() != null
                && (noxbox.getTimeOwnerVerified() == null || noxbox.getTimePartyVerified() == null)) {
            return moving;
        }

        if (noxbox.getTimePartyVerified() != null &&
                noxbox.getTimeOwnerVerified() != null &&
                noxbox.getTimeCompleted() == null) {

            return performing;
        }
        return initial;
    }
}
