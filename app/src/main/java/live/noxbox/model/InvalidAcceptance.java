package live.noxbox.model;

import live.noxbox.R;

public enum InvalidAcceptance {

    smile(R.drawable.ic_photo_smile, R.string.smile),
    eyes(R.drawable.ic_open_eyes, R.string.openYourEyes),
    face(R.drawable.ic_photo_smile, R.string.openYourFace),
    faceSize(R.drawable.ic_photo_face_close, R.string.approachFace),
    none(R.drawable.none, R.string.none);


    private int image;
    private int content;

    InvalidAcceptance(int image, int content) {
        this.image = image;
        this.content = content;
    }

    public int getImage() {
        return image;
    }

    public int getContent() {
        return content;
    }
}
