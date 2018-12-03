package live.noxbox.model;

import java.util.Map;

/**
 * Created by nicolay.lipnevich on 5/5/2018.
 */

public class NotificationData {

    private NotificationType type;
    private Integer icon;
    private Integer progress = 0;
    private Integer maxProgress = 0;
    private Boolean local = false;
    private String estimation, name, message, price, balance, previousBalance, time, id;
    private InvalidAcceptance invalidAcceptance;


    public static NotificationData create(Map<String, String> data) {
        // TODO (vl) create custom notification for pushes from server
        if (data.get("type") == null) return new NotificationData();

        return new NotificationData()
                .setId(data.get("id"))
                .setType(NotificationType.valueOf(data.get("type")))
                .setName(data.get("name"))
                .setMessage(data.get("message"))
                .setLocal(data.get("local") != null ? Boolean.valueOf(data.get("local")) : false)
                .setPrice(data.get("price"))
                .setBalance(data.get("balance") != null ? data.get("balance") : "0")
                .setPreviousBalance(data.get("previousBalance") != null ? data.get("previousBalance") : "0");
    }

    public Boolean getIgnore() {
        return getType() == null ||
                (getType() == NotificationType.balance && balance != null && balance.equals(previousBalance));
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationData setType(NotificationType type) {
        this.type = type;
        return this;
    }

    public String getEstimation() {
        return estimation;
    }

    public NotificationData setEstimation(String estimation) {
        this.estimation = estimation;
        return this;
    }

    public Integer getIcon() {
        return icon;
    }

    public NotificationData setIcon(Integer icon) {
        this.icon = icon;
        return this;
    }

    public String getName() {
        return name;
    }

    public NotificationData setName(String name) {
        this.name = name;
        return this;
    }

    public String getMessage() {
        if(message == null) message = "";
        return message;
    }

    public NotificationData setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getId() {
        return id;
    }

    public NotificationData setId(String id) {
        this.id = id;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public NotificationData setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getBalance() {
        return balance;
    }

    public NotificationData setBalance(String balance) {
        this.balance = balance;
        return this;
    }

    public Boolean getLocal() {
        return local;
    }

    public NotificationData setLocal(Boolean local) {
        this.local = local;
        return this;
    }

    public String getPreviousBalance() {
        return previousBalance;
    }

    public NotificationData setPreviousBalance(String previousBalance) {
        this.previousBalance = previousBalance;
        return this;
    }

    public Integer getProgress() {
        return this.progress;
    }

    public NotificationData setProgress(Integer progress) {
        this.progress = progress;
        return this;
    }

    public String getTime() {
        return time;
    }

    public NotificationData setTime(String time) {
        this.time = time;
        return this;
    }

    public InvalidAcceptance getInvalidAcceptance() {
        return invalidAcceptance;
    }

    public NotificationData setInvalidAccetrance(InvalidAcceptance invalidAcceptance) {
        this.invalidAcceptance = invalidAcceptance;
        return this;
    }

    public Integer getMaxProgress() {
        return maxProgress;
    }

    public NotificationData setMaxProgress(Integer maxProgress) {
        this.maxProgress = maxProgress;
        return this;
    }
}
