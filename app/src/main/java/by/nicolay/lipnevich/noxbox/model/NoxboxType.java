package by.nicolay.lipnevich.noxbox.model;

import by.nicolay.lipnevich.noxbox.payer.massage.R;

public enum NoxboxType {

    // activities
    massage(R.string.massage, R.drawable.masseur),
    tirefitting(R.string.tirefitting, R.drawable.masseur);

    private int message;
    private final int image;

    NoxboxType(int message, int image) {

        this.message = message;
        this.image = image;
    }

    public int getMessage() {
        return message;
    }

    public int getImage() {
        return image;
    }
}
