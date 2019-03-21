package live.noxbox.model;

import live.noxbox.R;

public enum NoxboxType {
    nanny(0, R.string.nanny, R.string.nanny, R.drawable.ic_nanny_supply, R.drawable.ic_nanny_demand, R.drawable.illustration_nanny, R.string.nannyDescription, R.string.nannyDuration, "60"),
    photographer(1, R.string.photographer, R.string.photographer, R.drawable.ic_photographer_supply, R.drawable.ic_photographer_demand, R.drawable.illustration_photographer, R.string.photographerDescription, R.string.photographerDuration, "60"),
    meditation(2, R.string.meditation, R.string.guru, R.drawable.ic_meditation_supply, R.drawable.ic_meditation_demand, R.drawable.illustration_meditation, R.string.meditationDescription, R.string.meditationDuration, "60"),
    water(3, R.string.water, R.string.waterSupplier, R.drawable.ic_water_supply, R.drawable.ic_water_demand, R.drawable.illustration_water, R.string.waterDescription, R.string.waterDuration, "60"),
    plant(4, R.string.plant, R.string.gardener, R.drawable.ic_plant_supply, R.drawable.ic_plant_demand, R.drawable.illustration_plant, R.string.plantDescription, R.string.plantDuration, "60"),
    hairdresser(5, R.string.haircut, R.string.hairdresser, R.drawable.ic_hairdresser_supply, R.drawable.ic_hairdresser_demand, R.drawable.illustration_hairdresser, R.string.haircutDescription, R.string.haircutDuration, "60"),
    massage(6, R.string.massage, R.string.masseur, R.drawable.ic_massage_supply, R.drawable.ic_massage_demand, R.drawable.illustration_massage, R.string.massageDescription, R.string.massageDuration, "60"),
    garbage(7, R.string.cleaning, R.string.cleaner, R.drawable.ic_cleaning_demand, R.drawable.ic_cleaning_demand, R.drawable.illustration_cleaning, R.string.cleaningDescription, R.string.cleaningDuration, "60"),
    redirect(8, R.string.redirectToPlayMarket, R.string.none, R.drawable.ic_play_market, R.drawable.ic_play_market, 0, 0, 0, "0");

    private int id;
    private int name;
    private int profession;
    private int imageSupply;
    private int imageDemand;
    private int illustration;
    private int description;
    private int duration;
    private String minutes;

    NoxboxType(int id, int name, int profession, int imageSupply, int imageDemand, int illustration, int description, int duration, String minutes) {
        this.id = id;
        this.name = name;
        this.profession = profession;
        this.imageSupply = imageSupply;
        this.imageDemand = imageDemand;
        this.illustration = illustration;
        this.description = description;
        this.duration = duration;
        this.minutes = minutes;
    }

    public int getName() {
        return name;
    }

    public int getImageSupply() {
        return imageSupply;
    }

    public int getImageDemand() {
        return imageDemand;
    }

    public int getIllustration() {
        return illustration;
    }

    public int getId() {
        return id;
    }

    public int getDuration() {
        return duration;
    }

    public String getMinutes() {
        return minutes;
    }

    public int getDescription() {
        return description;
    }


    public static NoxboxType byId(int id) {
        for (NoxboxType noxboxType : NoxboxType.values()) {
            if (noxboxType.id == id) {
                return noxboxType;
            }
        }
        return nanny;
    }

    public int getProfession() {
        return profession;
    }
}
