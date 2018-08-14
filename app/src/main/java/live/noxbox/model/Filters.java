package live.noxbox.model;

import java.util.List;

public class Filters {

    private Boolean supply;
    private Boolean demand;
    private Integer maxPrice;
    private List<NoxboxType> types;

    public Filters() { }

    public Filters(Boolean supply, Boolean demand, Integer price, List<NoxboxType> types) {
        this.supply = supply;
        this.demand = demand;
        this.maxPrice = price;
        this.types = types;
    }

    public Boolean getSupply() {
        return supply;
    }

    public Filters setSupply(Boolean supply) {
        this.supply = supply;
        return this;
    }

    public Boolean getDemand() {
        return demand;
    }

    public Filters setDemand(Boolean demand) {
        this.demand = demand;
        return this;
    }

    public Integer getPrice() {
        return maxPrice;
    }

    public Filters setPrice(Integer price) {
        this.maxPrice = price;
        return this;
    }

    public List<NoxboxType> getTypes() {
        return types;
    }

    public Filters setTypes(List<NoxboxType> types) {
        this.types = types;
        return this;
    }
}
