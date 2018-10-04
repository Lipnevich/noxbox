package live.noxbox.model;

import java.util.Map;

import live.noxbox.tools.InvalidAcceptance;

/**
 * Created by nicolay.lipnevich on 5/5/2018.
 */

public class Notification {

    private NotificationType type;
    private Integer id, icon;
    private Integer progress = 0;
    private Integer maxProgress = 0;
    private Boolean local = false;
    private String estimation, name, message, price, balance, previousBalance, time;
    private InvalidAcceptance invalidAcceptance;


    public static Notification create(Map<String, String> data) {
        // TODO (vl) create custom notification for pushes from server
        if (data.get("type") == null) return new Notification();

        return new Notification()
                .setType(NotificationType.valueOf(data.get("type")))
                .setName(data.get("name"))
                .setMessage(data.get("message"))
                .setLocal(data.get("local") != null ? Boolean.valueOf(data.get("local")) : false)
                .setPrice(data.get("price"))
                .setBalance(data.get("balance") != null ? data.get("balance") : "0")
                .setPreviousBalance(data.get("previousBalance") != null ? data.get("previousBalance") : "0");
    }

    public static Notification create(Event event) {
        return new Notification()
                .setType(event.getType())
                .setEstimation(event.getEstimationTime())
                //.setIcon(event.getSender().getPhoto())
                .setName(event.getSender().getName())
                .setMessage(event.getMessage())
                .setLocal(true);
    }

    public Boolean getIgnore() {
        return getType() == null ||
                (getType() == NotificationType.balance && balance != null && balance.equals(previousBalance));
    }

    public NotificationType getType() {
        return type;
    }

    public Notification setType(NotificationType type) {
        this.type = type;
        return this;
    }

    public String getEstimation() {
        return estimation;
    }

    public Notification setEstimation(String estimation) {
        this.estimation = estimation;
        return this;
    }

    public Integer getIcon() {
        return icon;
    }

    public Notification setIcon(Integer icon) {
        this.icon = icon;
        return this;
    }

    public String getName() {
        return name;
    }

    public Notification setName(String name) {
        this.name = name;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Notification setMessage(String message) {
        this.message = message;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public Notification setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public Notification setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getBalance() {
        return balance;
    }

    public Notification setBalance(String balance) {
        this.balance = balance;
        return this;
    }

    public Boolean getLocal() {
        return local;
    }

    public Notification setLocal(Boolean local) {
        this.local = local;
        return this;
    }

    public String getPreviousBalance() {
        return previousBalance;
    }

    public Notification setPreviousBalance(String previousBalance) {
        this.previousBalance = previousBalance;
        return this;
    }

    public Integer getProgress() {
        return this.progress;
    }

    public Notification setProgress(Integer progress) {
        this.progress = progress;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Notification setTime(String time) {
        this.time = time;
        return this;
    }

    public InvalidAcceptance getInvalidAcceptance() {
        return invalidAcceptance;
    }

    public Notification setInvalidAccetrance(InvalidAcceptance invalidAcceptance) {
        this.invalidAcceptance = invalidAcceptance;
        return this;
    }

    public Integer getMaxProgress() {
        return maxProgress;
    }

    public Notification setMaxProgress(Integer maxProgress) {
        this.maxProgress = maxProgress;
        return this;
    }
}
