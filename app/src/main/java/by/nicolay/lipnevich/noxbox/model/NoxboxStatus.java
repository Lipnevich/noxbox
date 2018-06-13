package by.nicolay.lipnevich.noxbox.model;

public enum NoxboxStatus {

    empty,
    created,
    requesting,
    accepting,
    moving,
    watching,
    performing,
    enjoying,
    completed;

    public static NoxboxStatus getStatus(Profile profile) {
        Noxbox current = profile.getCurrent();
        if(current == null) return empty;

        return empty;
    }

}
