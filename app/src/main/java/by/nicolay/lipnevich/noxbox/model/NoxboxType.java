package by.nicolay.lipnevich.noxbox.model;

import by.nicolay.lipnevich.noxbox.R;

public enum NoxboxType {

    massage(R.string.massage, R.drawable.masseur),
    haircut(R.string.massage, R.drawable.masseur),
    dinner(R.string.massage, R.drawable.masseur),
    plumber(R.string.massage, R.drawable.masseur),
    sportCompanion(R.string.massage, R.drawable.masseur),
    manicure(R.string.massage, R.drawable.masseur),
    sportCoach(R.string.massage, R.drawable.masseur),
    divingCoach(R.string.massage, R.drawable.masseur),
    computerRepairMan(R.string.massage, R.drawable.masseur),
    husbandForHour(R.string.massage, R.drawable.masseur),
    cleanRoom(R.string.massage, R.drawable.masseur),
    nanny(R.string.massage, R.drawable.masseur),
    dogWalker(R.string.massage, R.drawable.masseur);


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
