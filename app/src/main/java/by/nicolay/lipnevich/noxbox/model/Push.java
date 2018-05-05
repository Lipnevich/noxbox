package by.nicolay.lipnevich.noxbox.model;

/**
 * Created by nicolay.lipnevich on 5/2/2018.
 */

public class Push {

    private PushData data;
    private String to;
    private String role;

    public String getRole() {
        return role;
    }

    public Push setRole(String role) {
        this.role = role;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Push setTo(String to) {
        this.to = to;
        return this;
    }

    public PushData getData() {
        return data;
    }

    public Push setData(PushData data) {
        this.data = data;
        return this;
    }
}
