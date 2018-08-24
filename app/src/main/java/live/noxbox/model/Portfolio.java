package live.noxbox.model;

import java.util.ArrayList;
import java.util.List;

public class Portfolio {

    private List<String> certificates;
    private List<String> workSamples;
    private Rating rating;

    public Portfolio() {
        certificates = new ArrayList<>();
        workSamples = new ArrayList<>();
        rating = new Rating();
    }

    public Portfolio(List<String> certificates, List<String> workSamples, Rating rating) {
        this.certificates = certificates;
        this.workSamples = workSamples;
        this.rating = rating;
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

}
