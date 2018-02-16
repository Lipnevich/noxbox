package by.nicolay.lipnevich.noxbox.model;

import java.io.Serializable;

public class Wallet implements Serializable {

    private String balance;
    private String frozenMoney;
    private String address;

    public String getAddress() {
        return address;
    }

    public Wallet setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getBalance() {
        return balance;
    }

    public Wallet setBalance(String balance) {
        this.balance = balance;
        return this;
    }

    public String getFrozenMoney() {
        return frozenMoney;
    }

    public Wallet setFrozenMoney(String frozenMoney) {
        this.frozenMoney = frozenMoney;
        return this;
    }
}
