package by.nicolay.lipnevich.noxbox.model;

/**
 * Created by nicolay.lipnevich on 11/10/2017.
 */
public class AllRates {

    private Rating received;
    private Rating sent;

    public AllRates() {
        Rating zeroRating = new Rating().setDislikes(0l).setLikes(0l);
        this.received = zeroRating;
        this.sent = zeroRating;
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
}
