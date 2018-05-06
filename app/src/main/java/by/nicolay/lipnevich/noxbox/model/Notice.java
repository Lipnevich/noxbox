package by.nicolay.lipnevich.noxbox.model;

import java.util.Map;

/**
 * Created by nicolay.lipnevich on 5/5/2018.
 */

public class Notice {

    private MessageType type;
    private Integer id;
    private Boolean ignore;
    private String estimation, icon, name, message, price, balance;

    public static Notice create(Map<String, String> data) {
        return new Notice()
                .setType(MessageType.valueOf(data.get("type")))
                .setEstimation(data.get("estimation"))
                .setIcon(data.get("icon"))
                .setName(data.get("name"))
                .setMessage(data.get("message"))
                .setIgnore(data.get("ignore") != null ? Boolean.valueOf(data.get("ignore")) : false)
                .setPrice(data.get("price"))
                .setBalance(data.get("balance"));
    }

    public Boolean getIgnore() {
        return ignore;
    }

    public Notice setIgnore(Boolean ignore) {
        this.ignore = ignore;
        return this;
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
}
