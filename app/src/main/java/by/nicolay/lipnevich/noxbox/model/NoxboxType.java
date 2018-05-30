package by.nicolay.lipnevich.noxbox.model;

import by.nicolay.lipnevich.noxbox.R;

public enum NoxboxType {

    massage(R.string.massage, R.drawable.masseur);

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
