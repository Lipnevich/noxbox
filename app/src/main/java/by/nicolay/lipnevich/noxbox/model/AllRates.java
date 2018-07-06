package by.nicolay.lipnevich.noxbox.model;

/**
 * Created by nicolay.lipnevich on 11/10/2017.
 */
public class AllRates {

    private Rating received;
    private Rating sent;

    private Long notResponded;
    private Long canceled;

    public AllRates() {
        Rating zeroRating = new Rating().setDislikes(0l).setLikes(0l);
        this.received = zeroRating;
        this.sent = zeroRating;
        this.notResponded = 0l;
        this.canceled = 0l;
    }

    public Rating getReceived() {
        return received;
    }

    public AllRates setReceived(Rating received) {
        this.received = received;
        return this;
    }

    public Rating getSent() {
        return sent;
    }

    public AllRates setSent(Rating sent) {
        this.sent = sent;
        return this;
    }

    public Long getNotResponded() {
        return notResponded;
    }

    public AllRates setNotResponded(Long notResponded) {
        this.notResponded = notResponded;
        return this;
    }

    public Long getCanceled() {
        return canceled;
    }

    public AllRates setCanceled(Long canceled) {
        this.canceled = canceled;
        return this;
    }

}
