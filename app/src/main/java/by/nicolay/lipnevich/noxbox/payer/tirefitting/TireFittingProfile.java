package by.nicolay.lipnevich.noxbox.payer.tirefitting;

import java.util.ArrayList;
import java.util.List;

import by.nicolay.lipnevich.noxbox.model.Profile;

public class TireFittingProfile extends Profile {

    private List<Car> cars = new ArrayList<>();

    public List<Car> getCars() {
        return cars;
    }

    public TireFittingProfile setCars(List<Car> cars) {
        this.cars = cars;
        return this;
    }

    public Car getCurrentCar() {
        for(Car car : cars) if(car.isCurrent()) return car;
        return null;
    }
}
