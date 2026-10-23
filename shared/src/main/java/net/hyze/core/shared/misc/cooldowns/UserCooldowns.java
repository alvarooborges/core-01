package net.hyze.core.shared.misc.cooldowns;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.hyze.core.shared.misc.utils.DateUtils;
import net.hyze.core.shared.misc.utils.NumberUtils;
import net.hyze.core.shared.user.User;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

public class UserCooldowns {

    public static final Table<Integer, String, Long> COOLDOWNS = HashBasedTable.create();

    public static void start(User user, String key, long value, TimeUnit unit) {
        COOLDOWNS.put(user.getId(), key, System.currentTimeMillis() + unit.toMillis(value));
    }

    public static boolean end(User user, String key) {
        return COOLDOWNS.remove(user.getId(), key) != null;
    }

    public static boolean hasEnded(User user, String key) {
        int userId = user.getId();

        if (!COOLDOWNS.contains(userId, key)) {
            return true;
        }

        if (COOLDOWNS.get(userId, key) <= System.currentTimeMillis()) {
            COOLDOWNS.remove(userId, key);
            return true;
        }

        return false;
    }

    public static long getMillisLeft(User user, String key) {
        return hasEnded(user, key) ? 0L : COOLDOWNS.get(user.getId(), key) - System.currentTimeMillis();
    }

    public static int getSecondsLeft(User user, String key) {
        return hasEnded(user, key) ? 0 : ((int) TimeUnit.MILLISECONDS.toSeconds(getMillisLeft(user, key))) + 1;
    }

    @Deprecated
    public static String getFormattedTimeLeft(User user, String key) {
        return getFormattedTimeLeft(getMillisLeft(user, key));
    }

    @Deprecated
    public static String getFormattedTimeLeft(long millis) {
        return DateUtils.millisToHumanStr(millis);
    }
}
