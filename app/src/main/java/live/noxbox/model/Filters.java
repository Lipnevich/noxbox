package live.noxbox.model;

import java.util.HashMap;
import java.util.Map;

public class Filters {

    private Boolean supply = true;
    private Boolean demand = true;
    private String maxPrice = "0";
    private Map<String, Boolean> types = new HashMap<>();

    public Filters() {
    }

    public Filters(Boolean supply, boolean demand, String price, Map<String, Boolean> types) {
        this.supply = supply;
        this.demand = demand;
        this.maxPrice = price;
        this.types = types;
    }

    public Boolean getSupply() {
        if (supply == null) {
            supply = true;
        }
        return supply;
    }

    public Filters setSupply(Boolean supply) {
        this.supply = supply;
        return this;
    }

    public Boolean getDemand() {
        if (demand == null) {
            demand = true;
        }
        return demand;
    }

    public Filters setDemand(Boolean demand) {
        this.demand = demand;
        return this;
    }

    public String getPrice() {
        if (maxPrice == null) {
            maxPrice = "0";
        }
        return maxPrice;
    }

    public Filters setPrice(String price) {
        this.maxPrice = price;
        return this;
    }

    public Map<String, Boolean> getTypes() {
        return types;
    }

    public Filters setTypes(Map<String, Boolean> types) {
        this.types = types;
        return this;
    }
}
