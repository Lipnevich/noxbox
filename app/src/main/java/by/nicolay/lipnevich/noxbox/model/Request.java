package by.nicolay.lipnevich.noxbox.model;

public class Request {

    private String id;
    private RequestType type;
    private NoxboxType noxboxType;
    private String toAddress;
    private String reason;
    private Position position;
    private Profile performer;
    private Profile payer;
    private Noxbox noxbox;

    public String getId() {
        return id;
    }

    public Request setId(String id) {
        this.id = id;
        return this;
    }

    public RequestType getType() {
        return type;
    }

    public Request setType(RequestType type) {
        this.type = type;
        return this;
    }

    public NoxboxType getNoxboxType() {
        return noxboxType;
    }

    public Request setNoxboxType(NoxboxType noxboxType) {
        this.noxboxType = noxboxType;
        return this;
    }

    public String getToAddress() {
        return toAddress;
    }

    public Request setToAddress(String toAddress) {
        this.toAddress = toAddress;
        return this;
    }

    public Position getPosition() {
        return position;
    }

    public Request setPosition(Position position) {
        this.position = position;
        return this;
    }

    public Profile getPerformer() {
        return performer;
    }

    public Request setPerformer(Profile performer) {
        this.performer = performer;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public Request setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public Profile getPayer() {
        return payer;
    }

    public Request setPayer(Profile payer) {
        this.payer = payer;
        return this;
    }

    public Noxbox getNoxbox() {
        return noxbox;
    }

    public Request setNoxbox(Noxbox noxbox) {
        this.noxbox = noxbox;
        return this;
    }
}
