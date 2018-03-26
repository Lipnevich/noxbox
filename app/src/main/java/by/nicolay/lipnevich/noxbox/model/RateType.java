package by.nicolay.lipnevich.noxbox.model;

import by.nicolay.lipnevich.noxbox.performer.massage.R;

public enum RateType {

    // qualities
    responsibility(R.string.responsibility),
    accuracy(R.string.accuracy),
    politeness(R.string.politeness),
    kindness(R.string.kindness),
    sincerity(R.string.sincerity),
    wisdom(R.string.wisdom),
    humor(R.string.humor),
    patience(R.string.patience);

    private int resource;

    RateType(int resource) {
        this.resource = resource;
    }

    public int getResource() {
        return resource;
    }
}
