package by.nicolay.lipnevich.noxbox.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import static by.nicolay.lipnevich.noxbox.Configuration.MIN_RATE_IN_PERCENTAGE;

/**
 * Created by nicolay.lipnevich on 11/10/2017.
 */
public class Rating {

    private Integer receivedLikes = 0;
    private Integer receivedDislikes = 0;
    private Integer sentLikes = 0;
    private Integer sentDislikes = 0;
    private Integer notResponded = 0;
    private Integer canceled = 0;

    private Map<String, Comment> comments = new HashMap<>();

    public Rating() {
    }

    public Integer getReceivedLikes() {
        return receivedLikes;
    }

    public Rating setReceivedLikes(Integer receivedLikes) {
        this.receivedLikes = receivedLikes;
        return this;
    }

    public Integer getReceivedDislikes() {
        return receivedDislikes;
    }

    public Rating setReceivedDislikes(Integer receivedDislikes) {
        this.receivedDislikes = receivedDislikes;
        return this;
    }

    public Integer getSentLikes() {
        return sentLikes;
    }

    public Rating setSentLikes(Integer sentLikes) {
        this.sentLikes = sentLikes;
        return this;
    }

    public Integer getSentDislikes() {
        return sentDislikes;
    }

    public Rating setSentDislikes(Integer sentDislikes) {
        this.sentDislikes = sentDislikes;
        return this;
    }

    public Integer getNotResponded() {
        return notResponded;
    }

    public Rating setNotResponded(Integer notResponded) {
        this.notResponded = notResponded;
        return this;
    }

    public Integer getCanceled() {
        return canceled;
    }

    public Rating setCanceled(Integer canceled) {
        this.canceled = canceled;
        return this;
    }

    public Map<String, Comment> getComments() {
        return comments;
    }

    public Rating setComments(Map<String, Comment> comments) {
        this.comments = comments;
        return this;
    }

    @Exclude
    public int toPercentage() {
        int likes = this.receivedLikes;
        int dislikes = this.receivedDislikes;

        if (likes == 0 && dislikes == 0) return 100;
        if (likes < 10 && dislikes == 1) return MIN_RATE_IN_PERCENTAGE;
        if (likes == 0 && dislikes > 1) return 0;

        return (likes / (likes + dislikes)) * 100;
    }
}
