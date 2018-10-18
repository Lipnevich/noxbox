package live.noxbox.model;

public class Comment {
    private String id;
    private String text;
    private Long time;
    private Boolean like;

    public Comment(String id, String text, Long time, Boolean like) {
        this.id = id;
        this.text = text;
        this.time = time;
        this.like = like;
    }

    public Comment() {
    }

    public String getId() {
        return id;
    }

    public Comment setId(String id) {
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    public Comment setText(String text) {
        this.text = text;
        return this;
    }

    public Long getTime() {

        return time;
    }

    public Comment setTime(Long time) {
        this.time = time;
        return this;
    }

    public Boolean getLike() {
        if (like == null) {
            like = Boolean.FALSE;
        }
        return like;
    }

    public Comment setLike(Boolean like) {
        this.like = like;
        return this;
    }
}
