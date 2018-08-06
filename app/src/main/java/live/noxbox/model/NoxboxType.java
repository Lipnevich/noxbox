package live.noxbox.model;

import live.noxbox.R;

public enum NoxboxType {

    massage(0, R.string.massage, R.drawable.masseur, R.drawable.music_back, R.string.massageDescription, 30),
    haircut(1, R.string.haircut, R.drawable.haircut, R.drawable.music_back, R.string.haircutDescription, 30),
    dinner(2, R.string.dinner, R.drawable.dinner, R.drawable.music_back, R.string.dinnerDescription, 60),
    plumber(3, R.string.plumber, R.drawable.plumber, R.drawable.music_back, R.string.plumberDescription, 10),
    sportCompanion(4, R.string.sportCompanion, R.drawable.sport_companion, R.drawable.music_back, R.string.sportCompanionDescription, 60),
    manicure(5, R.string.manicure, R.drawable.manicure, R.drawable.music_back, R.string.manicureDescription, 40),
    sportCoach(6, R.string.sportCoach, R.drawable.sport_coach, R.drawable.music_back, R.string.sportCoachDescription, 60),
    computerRepairMan(7, R.string.computerRepairMan, R.drawable.computer_repair_man, R.drawable.music_back, R.string.computerRepairDescription, 120),
    husbandForHour(8, R.string.husbandForHour, R.drawable.plumber, R.drawable.music_back, R.string.husbandForHourDescription, 10),
    houseWife(9, R.string.houseWife, R.drawable.housewife, R.drawable.music_back, R.string.houseWifeDescription, 60),
    nanny(10, R.string.nanny, R.drawable.nanny, R.drawable.music_back, R.string.nannyDescription, 240),
    dogWalker(11, R.string.dogWalker, R.drawable.dog_walker, R.drawable.music_back, R.string.dogWalkingDescription, 40),
    homeTeacher(12, R.string.homeTeacher, R.drawable.home_teacher, R.drawable.music_back, R.string.homeTeacherDescription, 60),
    musician(13, R.string.musician, R.drawable.musician, R.drawable.music_back, R.string.musicianDescription, 45);
    //TODO (vl) please add the desired service
    //info(14, R.string.musician, R.drawable.musician, R.drawable.music_back, R.string.musicianDescription, 45);


    private int id;
    private int name;
    private final int image;
    private final int background;
    private int description;
    private int min;

    NoxboxType(int id, int name, int image, int background, int description, int min) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.background = background;
        this.description = description;
        this.min = min;
    }

    public int getName() {
        return name;
    }

    public int getImage() {
        return image;
    }

    public int getBackground() {
        return background;
    }

    public int getId() {
        return id;
    }

    public int getDescription() {
        return description;
    }

    public int getMin() {
        return min;
    }

    public NoxboxType setMin(int min) {
        this.min = min;
        return this;
    }

    public static NoxboxType byId(int id) {
        for (NoxboxType noxboxType : NoxboxType.values()) {
            if (noxboxType.id == id) {
                return noxboxType;
            }
        }
        return massage;
    }
}
