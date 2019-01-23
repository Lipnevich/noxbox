package live.noxbox.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private Map<String, List<String>> images = new HashMap<>();
    private Long timeCreated;

    public Portfolio(Map<String, List<String>> images, Long timeCreated) {
        this.images = images;
        this.timeCreated = timeCreated;
    }

    public Portfolio(Long timeCreated){
        this.timeCreated = timeCreated;
    }

    public Portfolio() { }

    public Map<String, List<String>> getImages() {
        return images;
    }

    public Portfolio setImages(Map<String, List<String>> images) {
        this.images = images;
        return this;
    }

    public Long getTimeCreated() {
        if(timeCreated == null){
            timeCreated = 0L;
        }
        return timeCreated;
    }

    public Portfolio setTimeCreated(Long timeCreated) {
        this.timeCreated = timeCreated;
        return this;
    }
}
