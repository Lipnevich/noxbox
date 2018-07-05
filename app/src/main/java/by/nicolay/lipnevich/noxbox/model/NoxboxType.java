package by.nicolay.lipnevich.noxbox.model;

import by.nicolay.lipnevich.noxbox.R;

public enum NoxboxType{

    massage(0,R.string.massage, R.drawable.masseur),
    haircut(1,R.string.haircut, R.drawable.haircut),
    dinner(2,R.string.dinner, R.drawable.dinner),
    plumber(3,R.string.plumber, R.drawable.plumber),
    sportCompanion(4,R.string.sportCompanion, R.drawable.sport_companion),
    manicure(5,R.string.manicure, R.drawable.manicure),
    sportCoach(6,R.string.sportCoach, R.drawable.sport_coach),
    computerRepairMan(7,R.string.computerRepairMan, R.drawable.computer_repair_man),
    husbandForHour(8,R.string.husbandForHour, R.drawable.plumber),
    houseWife(9,R.string.houseWife, R.drawable.housewife),
    nanny(10,R.string.nanny, R.drawable.nanny),
    dogWalker(11,R.string.dogWalker, R.drawable.dog_walker),
    homeTeacher(12,R.string.homeTeacher, R.drawable.home_teacher);


    private int id;
    private int name;
    private final int image;

    NoxboxType(int id,int name, int image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public int getName() {
        return name;
    }

    public int getImage() {
        return image;
    }

    public int getId() {
        return id;
    }

    public static NoxboxType byId(int id){
        for (NoxboxType noxboxType : NoxboxType.values()) {
            if (noxboxType.id == id) {
                return noxboxType;
            }
        }
        return massage;
    }
}
