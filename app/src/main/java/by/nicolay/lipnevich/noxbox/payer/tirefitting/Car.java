package by.nicolay.lipnevich.noxbox.payer.tirefitting;

import java.io.Serializable;

/**
 * Created by nicolay.lipnevich on 21/05/2017.
 */
public class Car implements Serializable {

    private String manufacturer;
    private String model;
    private String size;
    private boolean current;

    public String getManufacturer() {
        return manufacturer;
    }

    public Car setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    public String getModel() {
        return model;
    }

    public Car setModel(String model) {
        this.model = model;
        return this;
    }

    public String getSize() {
        return size;
    }

    public Car setSize(String size) {
        this.size = size;
        return this;
    }

    public boolean isCurrent() {
        return current;
    }

    public Car setCurrent(boolean current) {
        this.current = current;
        return this;
    }

    @Override
    public String toString() {
        return manufacturer + " " + model + " " + size;
    }
}
