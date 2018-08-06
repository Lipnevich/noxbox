package live.noxbox.model;

/**
 * Created by nicolay.lipnevich on 5/2/2018.
 */

public class Push {

    private String recipientId, type, estimation, icon, name, message, price, balance;

    public String getIcon() {
        return icon;
    }

    public Push setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getType() {
        return type;
    }

    public Push setType(String type) {
        this.type = type;
        return this;
    }

    public String getEstimation() {
        return estimation;
    }

    public Push setEstimation(String estimation) {
        this.estimation = estimation;
        return this;
    }

    public String getName() {
        return name;
    }

    public Push setName(String name) {
        this.name = name;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Push setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public Push setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getBalance() {
        return balance;
    }

    public Push setBalance(String balance) {
        this.balance = balance;
        return this;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public Push setRecipientId(String recipientId) {
        this.recipientId = recipientId;
        return this;
    }
}
