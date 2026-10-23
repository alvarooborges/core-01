package net.hyze.core.spigot.misc.notification.actionbar;

import lombok.Getter;
import lombok.Setter;
import net.hyze.core.spigot.CoreSpigotPlugin;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

@Setter
@Getter
public abstract class ActionBarNotification implements Supplier<String> {

    private String currentNotificationText;
    private long currentNotificationTicks;

    public ActionBarNotification(final Player player) {
        new ActionBarRunnable(player, this)
                .runTaskTimerAsynchronously(CoreSpigotPlugin.getInstance(), 20, 15);
    }

    public void sendNotification(String message) {
        this.currentNotificationText = message;
        this.currentNotificationTicks = 0;
    }
}
