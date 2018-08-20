package live.noxbox.model;

import java.util.List;

public class Portfolio {

    private List<String> certificates;
    private List<String> workSamples;

    public Portfolio() { }

    public Portfolio(List<String> certificates, List<String> workSamples) {
        this.certificates = certificates;
        this.workSamples = workSamples;
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
}
