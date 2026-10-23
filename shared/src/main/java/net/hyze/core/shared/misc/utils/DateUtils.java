package net.hyze.core.shared.misc.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    private static final SimpleDateFormat PRECISE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S z");

    public static String toString(Date date) {
        return PRECISE_FORMATTER.format(date);
    }

    public static Date fromString(String input) {
        try {
            return PRECISE_FORMATTER.parse(input);
        } catch (ParseException ex) {
            return null;
        }
    }


    public static String diffToDigitalStr(Date date1, Date date2) {
        return diffToDigitalStr(date1, date2, false, false);
    }

    public static String diffToDigitalStr(Date date1, Date date2, boolean showZeroMinutes) {
        return diffToDigitalStr(date1, date2, showZeroMinutes, false);
    }

    public static String diffToDigitalStr(Date date1, Date date2, boolean showZeroMinutes, boolean showZeroHours) {
        long diff = date2.getTime() - date1.getTime();

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);

        StringBuilder builder = new StringBuilder();

        boolean withHours = diffHours > 0 || showZeroHours;
        boolean withMinutes = diffHours > 0 || diffMinutes > 0 || showZeroMinutes;

        if (withHours) {
            builder.append(String.format(
                    "%02d", diffHours
            ));
        }

        if (withMinutes) {
            builder.append(String.format(
                    "%s%02d",
                    withHours ? ":" : "",
                    diffMinutes
            ));
        }

        builder.append(String.format(
                "%s%02d",
                withMinutes ? ":" : "",
                diffSeconds
        ));

        return builder.toString();
    }

    public static String millisToHumanStr(long millis) {
        if (millis < 0) {
            return "0s";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);

        if (days > 0) {
            sb.append(days).append("d ");
        }

        if (hours > 0) {
            sb.append(hours).append("h ");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }

        if (days == 0 && hours == 0 && minutes == 0 && seconds == 1 && millis < 1000) {

            double halfsec = millis / 1000D;

            NumberFormat nf = new DecimalFormat("#.##");
            String value = nf.format(NumberUtils.roundDouble(halfsec, 2));

            sb.append(value).append("s");

        } else if (seconds > 0) {

            sb.append(seconds).append("s");

        }

        return (sb.toString().trim());
    }
}
