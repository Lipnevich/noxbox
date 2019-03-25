package live.noxbox.model;

import java.util.HashMap;
import java.util.Map;

import static live.noxbox.model.Noxbox.isNullOrZero;

/**
 * Created by nicolay.lipnevich on 11/10/2017.
 */
public class Rating {

    private Integer receivedLikes;
    private Integer receivedDislikes;
    private Integer sentLikes;
    private Integer sentDislikes;

    private Integer notResponded;
    private Integer canceled;
    private Integer notVerified;

    //key is Profile.id
    private Map<String, Comment> comments = new HashMap<>();

    public Rating() {
    }

    public Integer getReceivedLikes() {
        if (isNullOrZero(receivedLikes) || receivedLikes < 0) {
            setReceivedLikes(0);
        }
        return receivedLikes;
    }

    public Rating setReceivedLikes(Integer receivedLikes) {
        this.receivedLikes = receivedLikes;
        return this;
    }

    public Integer getReceivedDislikes() {
        if (isNullOrZero(receivedDislikes) || receivedDislikes < 0) {
            setReceivedDislikes(0);
        }
        return receivedDislikes;
    }

    public Rating setReceivedDislikes(Integer receivedDislikes) {
        this.receivedDislikes = receivedDislikes;
        return this;
    }

    public Integer getSentLikes() {
        if (isNullOrZero(sentLikes) || sentLikes < 0) {
            setSentLikes(0);
        }
        return sentLikes;
    }

    public Rating setSentLikes(Integer sentLikes) {
        this.sentLikes = sentLikes;
        return this;
    }

    public Integer getSentDislikes() {
        if (isNullOrZero(sentDislikes) || sentDislikes < 0) {
            setSentDislikes(0);
        }
        return sentDislikes;
    }

    public Rating setSentDislikes(Integer sentDislikes) {
        this.sentDislikes = sentDislikes;
        return this;
    }

    public Integer getNotResponded() {
        if (isNullOrZero(notResponded) || notResponded < 0) {
            setNotResponded(0);
        }
        return notResponded;
    }

    public Rating setNotResponded(Integer notResponded) {
        this.notResponded = notResponded;
        return this;
    }

    public Integer getCanceled() {
        if (isNullOrZero(canceled) || canceled < 0) {
            setCanceled(0);
        }
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
        if (isNullOrZero(notVerified) || notVerified < 0) {
            setNotVerified(0);
        }
        return notVerified;
    }

    public Rating setNotVerified(Integer notVerified) {
        this.notVerified = notVerified;
        return this;
    }
}
