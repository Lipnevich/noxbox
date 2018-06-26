package by.nicolay.lipnevich.noxbox.tools;

public class TimeFormatter {
    public static String getTime(String time) {
        int result = (Integer.parseInt(time) / 60);

        return "" + result + " мин.";
    }
}
