package live.noxbox.model;

import java.util.ArrayList;
import java.util.List;

public class Portfolio {

    private List<String> certificates;
    private List<String> workSamples;
    private Rating rating;
    private int typeId;

    public Portfolio() {
        certificates = new ArrayList<>();
        workSamples = new ArrayList<>();
        rating = new Rating();
    }

    public Portfolio(List<String> certificates, List<String> workSamples, Rating rating,int typeId) {
        this.certificates = certificates;
        this.workSamples = workSamples;
        this.rating = rating;
        this.typeId = typeId;
    }

    public List<String> getCertificates() {
        return certificates;
    }

    public Portfolio setCertificates(List<String> certificates) {
        this.certificates = certificates;
        return this;
    }

    public List<String> getWorkSamples() {
        return workSamples;
    }

    public Portfolio setWorkSamples(List<String> workSamples) {
        this.workSamples = workSamples;
        return this;
    }

    public Rating getRating() {
        return rating;
    }

    public Portfolio setRating(Rating rating) {
        this.rating = rating;
        return this;
    }

    public int getTypeId() {
        return typeId;
    }

    public Portfolio setTypeId(int typeId) {
        this.typeId = typeId;
        return this;
    }
}
