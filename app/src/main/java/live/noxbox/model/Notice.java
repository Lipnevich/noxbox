package live.noxbox.model;

import java.util.Map;

/**
 * Created by nicolay.lipnevich on 5/5/2018.
 */

public class Notice {

    private EventType type;
    private Integer id, progress,time;
    private Boolean local = false;
    private String estimation, icon, name, message, price, balance, previousBalance;

    public static Notice create(Map<String, String> data) {
        if (data.get("type") == null) return new Notice();

        return new Notice()
                .setType(EventType.valueOf(data.get("type")))
                .setEstimation(data.get("estimation"))
                .setIcon(data.get("icon"))
                .setName(data.get("name"))
                .setMessage(data.get("message"))
                .setLocal(data.get("local") != null ? Boolean.valueOf(data.get("local")) : false)
                .setPrice(data.get("price"))
                .setBalance(data.get("balance") != null ? data.get("balance") : "0")
                .setPreviousBalance(data.get("previousBalance") != null ? data.get("previousBalance") : "0");
    }

    public static Notice create(Event event) {
        return new Notice()
                .setType(event.getType())
                .setEstimation(event.getEstimationTime())
                .setIcon(event.getSender().getPhoto())
                .setName(event.getSender().getName())
                .setMessage(event.getMessage())
                .setLocal(true);
    }

    public Boolean getIgnore() {
        return getType() == null ||
                (getType() == EventType.balance && balance != null && balance.equals(previousBalance));
    }

    public EventType getType() {
        return type;
    }

    public Notice setType(EventType type) {
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

    public Integer getProgress() {
        return this.progress;
    }

    public Notice setProgress(Integer progress) {
        this.progress = progress;
        return this;
    }

    public Integer getTime() {
        return time;
    }

    public Notice setTime(Integer time) {
        this.time = time;
        return this;
    }
}
