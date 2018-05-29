package by.nicolay.lipnevich.noxbox.model;

import java.util.Map;

/**
 * Created by nicolay.lipnevich on 5/5/2018.
 */

public class Notice {

    private MessageType type;
    private Integer id;
    private Boolean local = false;
    private String estimation, icon, name, message, price, balance, previousBalance;

    public static Notice create(Map<String, String> data) {
        if(data.get("type") == null) return new Notice();

        return new Notice()
                .setType(MessageType.valueOf(data.get("type")))
                .setEstimation(data.get("estimation"))
                .setIcon(data.get("icon"))
                .setName(data.get("name"))
                .setMessage(data.get("message"))
                .setLocal(data.get("local") != null ? Boolean.valueOf(data.get("local")) : false)
                .setPrice(data.get("price"))
                .setBalance(data.get("balance") != null ? data.get("balance") : "0")
                .setPreviousBalance(data.get("previousBalance") != null ? data.get("previousBalance") : "0");
    }

    public static Notice create(Message message) {
        return new Notice()
                .setType(message.getType())
                .setEstimation(message.getEstimationTime())
                .setIcon(message.getSender().getPhoto())
                .setName(message.getSender().getName())
                .setMessage(message.getStory())
                .setLocal(true);
    }

    public Boolean getIgnore() {
        return getType() == null ||
                (getType() == MessageType.balanceUpdated && balance != null && balance.equals(previousBalance));
    }

    public MessageType getType() {
        return type;
    }

    public Notice setType(MessageType type) {
        this.type = type;
        return this;
    }

    public String getEstimation() {
        return estimation;
    }

    public Notice setEstimation(String estimation) {
        this.estimation = estimation;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public Notice setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getName() {
        return name;
    }

    public Notice setName(String name) {
        this.name = name;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Notice setMessage(String message) {
        this.message = message;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public Notice setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public Notice setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getBalance() {
        return balance;
    }

    public Notice setBalance(String balance) {
        this.balance = balance;
        return this;
    }

    public Boolean getLocal() {
        return local;
    }

    public Notice setLocal(Boolean local) {
        this.local = local;
        return this;
    }

    public String getPreviousBalance() {
        return previousBalance;
    }

    public Notice setPreviousBalance(String previousBalance) {
        this.previousBalance = previousBalance;
        return this;
    }
}
