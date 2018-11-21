package live.noxbox.model;

import live.noxbox.R;

public enum MarketRole {
    demand(0, R.string.demand, "payerId"),
    supply(1, R.string.supply, "performerId");

    MarketRole(int id, int name, String ownerFieldId) {

        this.id = id;
        this.name = name;
        this.ownerFieldId = ownerFieldId;
    }

    private int id;
    private int name;
    private String ownerFieldId;

    public int getId() {
        return id;
    }

    public int getName() {
        return name;
    }

    public String getOwnerFieldId() {
        return ownerFieldId;
    }

    public static MarketRole byId(int id) {
        for (MarketRole role : MarketRole.values()) {
            if (role.id == id) {
                return role;
            }
        }
        return demand;
    }
}
