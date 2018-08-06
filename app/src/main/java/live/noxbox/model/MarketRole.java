package live.noxbox.model;

import live.noxbox.R;

public enum MarketRole {
    demand(0, R.string.demand),
    supply(1, R.string.supply);

    MarketRole(int id, int name) {

        this.id = id;
        this.name = name;
    }

    private int id;
    private final int name;

    public int getId() {
        return id;
    }

    public int getName() {
        return name;
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
