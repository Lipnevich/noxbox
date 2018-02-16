package by.nicolay.lipnevich.noxbox.model;

import java.io.Serializable;

public class Rating implements Serializable {

    private Long likes;
    private Long dislikes;

    public Long getLikes() {
        return likes;
    }

    public Rating setLikes(Long likes) {
        this.likes = likes;
        return this;
    }

    public Long getDislikes() {
        return dislikes;
    }

    public Rating setDislikes(Long dislikes) {
        this.dislikes = dislikes;
        return this;
    }

    public static Rating of(Long likes) {
        return new Rating().setLikes(likes).setDislikes(0l);
    }
}
