package live.noxbox.tools;

import live.noxbox.R;

public interface InvalidAcceptance {
    int getCorrectionMessage();

    class Smile implements InvalidAcceptance {

        @Override
        public int getCorrectionMessage() {
            return R.string.smile;
        }
    }

    class Eyes implements InvalidAcceptance {

        @Override
        public int getCorrectionMessage() {
            return R.string.openYourEyes;
        }
    }

    class Nose implements InvalidAcceptance {
        @Override
        public int getCorrectionMessage() {
            return R.string.openYourFace;
        }
    }

    class Face implements InvalidAcceptance {
        @Override
        public int getCorrectionMessage() {
            return R.string.approachFace;
        }
    }

    class None implements InvalidAcceptance {
        @Override
        public int getCorrectionMessage() {
            return R.string.none;
        }
    }
}



