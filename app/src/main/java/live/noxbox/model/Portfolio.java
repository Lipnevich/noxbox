package live.noxbox.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private Map<String,List<String>> images = new HashMap<>();

    public Portfolio(Map<String, List<String>> images) {
        this.images = images;
    }

    public Portfolio() {
    }

    public Map<String, List<String>> getImages() {
        return images;
    }

    public Portfolio setImages(Map<String, List<String>> images) {
        this.images = images;
        return this;
    }

}
