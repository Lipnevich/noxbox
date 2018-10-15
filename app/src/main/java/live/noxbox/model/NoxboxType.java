package live.noxbox.model;

import live.noxbox.R;

public enum NoxboxType {
    nanny(0, R.string.nanny, R.drawable.ic_nanny, R.drawable.illustration_nanny, R.string.nannyDescription, "60"),
    photographer(1, R.string.photographer, R.drawable.ic_photographer, R.drawable.illustration_photographer, R.string.photographerDescription, "60"),
    meditation(2, R.string.meditation, R.drawable.ic_meditation, R.drawable.illustration_meditation, R.string.meditationDescription, "60");
    //haircut(2, R.string.haircut, R.drawable.haircut, R.drawable.music_back, R.string.haircutDescription, "60"),
    //manicure(3, R.string.manicure, R.drawable.manicure, R.drawable.music_back, R.string.manicureDescription, "60");
    //massage(0, R.string.massage, R.drawable.masseur, R.drawable.music_back, R.string.massageDescription, 60),
    //dinner(2, R.string.dinner, R.drawable.dinner, R.drawable.music_back, R.string.dinnerDescription, 60),
    //plumber(3, R.string.plumber, R.drawable.plumber, R.drawable.music_back, R.string.plumberDescription, 60),
    //sportCompanion(4, R.string.sportCompanion, R.drawable.sport_companion, R.drawable.music_back, R.string.sportCompanionDescription, 60),
    //sportCoach(6, R.string.sportCoach, R.drawable.sport_coach, R.drawable.music_back, R.string.sportCoachDescription, 60),
    //computerRepairMan(7, R.string.computerRepairMan, R.drawable.computer_repair_man, R.drawable.music_back, R.string.computerRepairDescription, 60),
    //husbandForHour(8, R.string.husbandForHour, R.drawable.plumber, R.drawable.music_back, R.string.husbandForHourDescription, 60),
    //houseWife(9, R.string.houseWife, R.drawable.housewife, R.drawable.music_back, R.string.houseWifeDescription, 60),
    //dogWalker(11, R.string.dogWalker, R.drawable.dog_walker, R.drawable.music_back, R.string.dogWalkingDescription, 60),
    //homeTeacher(12, R.string.homeTeacher, R.drawable.home_teacher, R.drawable.music_back, R.string.homeTeacherDescription, 60),
    //musician(13, R.string.musician, R.drawable.musician, R.drawable.music_back, R.string.musicianDescription, 60);
    //evacuator(14, R.string.evacuator, R.drawable.evacuator, R.drawable.evacuator_back, R.string.evacuatorDescription, 60);
    //carBatteryRecharge(14, R.string.carBatteryRecharge, R.drawable.carBatteryRecharge, R.drawable.carBatteryRecharge_back, R.string.carBatteryRechargeDescription, 60);


    private int id;
    private int name;
    private final int image;
    private final int illustration;
    private int description;
    private String duration;

    NoxboxType(int id, int name, int image, int illustration, int description, String duration) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.illustration = illustration;
        this.description = description;
        this.duration = duration;
    }

    public int getName() {
        return name;
    }

    public int getImage() {
        return image;
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

    public String getDuration() {
        return duration;
    }

    public static NoxboxType byId(int id) {
        for (NoxboxType noxboxType : NoxboxType.values()) {
            if (noxboxType.id == id) {
                return noxboxType;
            }
        }
        return nanny;
    }
}
