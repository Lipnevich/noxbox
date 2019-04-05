package live.noxbox.model;

import live.noxbox.R;

public enum NoxboxType {
    nanny(0, R.string.nanny, R.string.nanny, R.drawable.ic_nanny_supply, R.drawable.ic_nanny_demand, R.drawable.illustration_nanny, R.string.nannyDescription),
    photographer(1, R.string.photographer, R.string.photographer, R.drawable.ic_photographer_supply, R.drawable.ic_photographer_demand, R.drawable.illustration_photographer, R.string.photographerDescription),
    meditation(2, R.string.meditation, R.string.guru, R.drawable.ic_meditation_supply, R.drawable.ic_meditation_demand, R.drawable.illustration_meditation, R.string.meditationDescription),
    water(3, R.string.water, R.string.waterSupplier, R.drawable.ic_water_supply, R.drawable.ic_water_demand, R.drawable.illustration_water, R.string.waterDescription),
    plant(4, R.string.plant, R.string.gardener, R.drawable.ic_plant_supply, R.drawable.ic_plant_demand, R.drawable.illustration_plant, R.string.plantDescription),
    hairdresser(5, R.string.haircut, R.string.hairdresser, R.drawable.ic_hairdresser_supply, R.drawable.ic_hairdresser_demand, R.drawable.illustration_hairdresser, R.string.haircutDescription),
    massage(6, R.string.massage, R.string.masseur, R.drawable.ic_massage_supply, R.drawable.ic_massage_demand, R.drawable.illustration_massage, R.string.massageDescription),
    cleaning(7, R.string.cleaning, R.string.cleaner, R.drawable.ic_cleaning_demand, R.drawable.ic_cleaning_demand, R.drawable.illustration_cleaning, R.string.cleaningDescription),
    redirect(8, R.string.redirectToPlayMarket, R.string.none, R.drawable.ic_play_market, R.drawable.ic_play_market, 0, 0);

    private int id;
    private int name;
    private int profession;
    private int imageSupply;
    private int imageDemand;
    private int illustration;
    private int description;

    NoxboxType(int id, int name, int profession, int imageSupply, int imageDemand, int illustration, int description) {
        this.id = id;
        this.name = name;
        this.profession = profession;
        this.imageSupply = imageSupply;
        this.imageDemand = imageDemand;
        this.illustration = illustration;
        this.description = description;
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
