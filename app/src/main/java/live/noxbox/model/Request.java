package live.noxbox.model;

public class Request {

    private String id;
    private NotificationType type;
    private String address;
    private String message;
    private Position position;
    private Noxbox noxbox;
    private Push push;

    public String getId() {
        return id;
    }

    public Request setId(String id) {
        this.id = id;
        return this;
    }

    public NotificationType getType() {
        return type;
    }

    public Request setType(NotificationType type) {
        this.type = type;
        return this;
    }

    public Position getPosition() {
        return position;
    }

    public Request setPosition(Position position) {
        this.position = position;
        return this;
    }

    public Noxbox getNoxbox() {
        return noxbox;
    }

    public Request setNoxbox(Noxbox noxbox) {
        this.noxbox = noxbox;
        return this;
    }

    public Push getPush() {
        return push;
    }

    public Request setPush(Push push) {
        this.push = push;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Request setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Request setAddress(String address) {
        this.address = address;
        return this;
    }
}
