package live.noxbox.model;

import java.util.HashMap;
import java.util.Map;

public class Filters {

    private Boolean supply = true;
    private Boolean demand = true;
    private Boolean allowNovices = true;
    private Integer price = Integer.MAX_VALUE;
    private Map<String, Boolean> types = new HashMap<>();

    {
        for (NoxboxType type : NoxboxType.values()) {
            types.put(type.name(), true);
        }
    }

    public Filters() {
    }

    public Filters(Boolean supply, boolean demand, Integer price, Map<String, Boolean> types) {
        this.supply = supply;
        this.demand = demand;
        this.price = price;
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

    public Integer getPrice() {
        if (price == null) {
            price = 0;
        }
        return price;
    }

    public Filters setPrice(Integer price) {
        this.price = price;
        return this;
    }

    public Map<String, Boolean> getTypes() {
        return types;
    }

    public Filters setTypes(Map<String, Boolean> types) {
        this.types = types;
        return this;
    }

    public Boolean getAllowNovices() {
        return allowNovices;
    }

    public Filters setAllowNovices(Boolean allowNovices) {
        this.allowNovices = allowNovices;
        return this;
    }

}
