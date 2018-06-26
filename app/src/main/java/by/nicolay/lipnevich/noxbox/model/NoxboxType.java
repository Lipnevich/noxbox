package by.nicolay.lipnevich.noxbox.model;

import by.nicolay.lipnevich.noxbox.R;

public enum NoxboxType {

    massage(R.string.massage, R.drawable.masseur),
    haircut(R.string.haircut, R.drawable.haircut),
    dinner(R.string.dinner, R.drawable.dinner),
    plumber(R.string.plumber, R.drawable.plumber),
    sportCompanion(R.string.sportCompanion, R.drawable.sport_companion),
    manicure(R.string.manicure, R.drawable.manicure),
    sportCoach(R.string.sportCoach, R.drawable.sport_coach),
    computerRepairMan(R.string.computerRepairMan, R.drawable.computer_repair_man),
    husbandForHour(R.string.husbandForHour, R.drawable.plumber),
    houseWife(R.string.houseWife, R.drawable.housewife),
    nanny(R.string.nanny, R.drawable.nanny),
    dogWalker(R.string.dogWalker, R.drawable.dog_walker),
    homeTeacher(R.string.homeTeacher,R.drawable.home_teacher);

    private int name;
    private final int image;

    NoxboxType(int name, int image) {

        this.name = name;
        this.image = image;
    }

    public int getName() {
        return name;
    }

    public int getImage() {
        return image;
    }
}
