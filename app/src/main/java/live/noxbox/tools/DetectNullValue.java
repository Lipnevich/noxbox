package live.noxbox.tools;

public abstract class DetectNullValue {

    public static boolean areNotTheyNull(Object... arg) {
        for (Object object : arg) {
            if (object == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean areTheyNull(Object... arg) {
        for (Object object : arg) {
            if (object != null) {
                return false;
            }
        }
        return true;
    }

}
