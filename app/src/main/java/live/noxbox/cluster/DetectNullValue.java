package live.noxbox.cluster;

public abstract class DetectNullValue {

    public static boolean areNotTheyNull(Object... arg) {
        for (Object object : arg) {
            if (object == null || (object instanceof Long && (Long) object == 0)) {
                return false;
            }
        }
        return true;
    }

    public static boolean areTheyNull(Object... arg) {
        for (Object object : arg) {
            if (object != null || (object instanceof Long && (Long) object > 0)) {
                return false;
            }
        }
        return true;
    }

}
