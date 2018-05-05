package by.nicolay.lipnevich.noxbox.model;

/**
 * Created by nicolay.lipnevich on 5/2/2018.
 */

public class PushData {

    private String type, estimation, icon, name, message, price, balance;

    public String getIcon() {
        return icon;
    }

    public PushData setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getType() {
        return type;
    }

    public PushData setType(String type) {
        this.type = type;
        return this;
    }

    public String getEstimation() {
        return estimation;
    }

    public PushData setEstimation(String estimation) {
        this.estimation = estimation;
        return this;
    }

    public String getName() {
        return name;
    }

    public PushData setName(String name) {
        this.name = name;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public PushData setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public PushData setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getBalance() {
        return balance;
    }

    public PushData setBalance(String balance) {
        this.balance = balance;
        return this;
    }
}
