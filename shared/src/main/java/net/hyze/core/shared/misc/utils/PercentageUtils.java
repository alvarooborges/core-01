package net.hyze.core.shared.misc.utils;

public class PercentageUtils {

    public static String getProgressBar(int current, int total, int sizeBar, String charFull, String charEmpty, String colorFull, String colorEmpty) {
        return getProgressBar((double) current, (double) total, sizeBar, charFull, charEmpty, colorFull, colorEmpty);
    }

    public static String getProgressBar(double current, double total, int sizeBar, String charFull, String charEmpty, String colorFull, String colorEmpty) {

        double percent = ((double) current / total) * 100;
        double percentChar = (double) 100 / sizeBar;
        StringBuilder sb = new StringBuilder();
        double restPercentage = percent;

        boolean alreadyFull = false;
        boolean alreadyEmpty = false;

        for (int i = 0; i < sizeBar; i++) {
            if (restPercentage >= percentChar) {
                if (!alreadyFull) {
                    sb.append(colorFull);
                    alreadyFull = true;
                }
                sb.append(charFull);
                restPercentage -= percentChar;
            } else {
                if (!alreadyEmpty) {
                    sb.append(colorEmpty);
                    alreadyEmpty = true;
                }
                sb.append(charEmpty);
            }
        }
        return sb.toString();
    }

    public static double getPercentage(double total, double current) {
        return (current * 100D) / total;
    }

}
