package live.noxbox.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nicolay.lipnevich on 11/10/2017.
 */
public class Rating {

    private int receivedLikes;
    private int receivedDislikes;
    private int sentLikes;
    private int sentDislikes;

    private int notResponded;
    private int canceled;
    private int notVerified;

    //key is Profile.id
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

    public Integer getNotVerified() {
        return notVerified;
    }

    public Rating setNotVerified(Integer notVerified) {
        this.notVerified = notVerified;
        return this;
    }
}
