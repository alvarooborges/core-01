package net.hyze.core.shared.misc.cooldowns;

import com.google.common.collect.Maps;
import net.hyze.core.shared.misc.utils.DateUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Cooldowns {

    public static final Map<String, Long> COOLDOWNS = Maps.newConcurrentMap();

    public static void start(String key, long value, TimeUnit unit) {
        COOLDOWNS.put(key, System.currentTimeMillis() + unit.toMillis(value));
    }

    public static boolean end(String key) {
        return COOLDOWNS.remove(key) != null;
    }

    public static boolean hasEnded(String key) {

        if (!COOLDOWNS.containsKey(key)) {
            return true;
        }

        Long storedCooldown = COOLDOWNS.get(key);
        if (storedCooldown != null && storedCooldown <= System.currentTimeMillis()) {
            COOLDOWNS.remove(key);
            return true;
        }

        return false;
    }

    public static long getMillisLeft(String key) {
        return hasEnded(key) ? 0L : COOLDOWNS.get(key) - System.currentTimeMillis();
    }

    public static int getSecondsLeft(String key) {
        return hasEnded(key) ? 0 : ((int) TimeUnit.MILLISECONDS.toSeconds(getMillisLeft(key))) + 1;
    }

    public static String getFormattedTimeLeft(String key) {
        return getFormattedTimeLeft(getMillisLeft(key));
    }

    @Deprecated
    public static String getFormattedTimeLeft(long millis) {
        return DateUtils.millisToHumanStr(millis);
    }
}
