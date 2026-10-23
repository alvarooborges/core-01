package net.hyze.core.spigot.misc.notification.actionbar;

import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.misc.cooldowns.Cooldowns;
import net.hyze.core.spigot.misc.utils.ActionBarMessage;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActionBarRunnable extends BukkitRunnable {

    private volatile static boolean enabled;

    private final WeakReference<Player> playerWeakReference;
    private final ActionBarNotification bar;

    public ActionBarRunnable(Player player, ActionBarNotification bar) {
        this.playerWeakReference = new WeakReference<>(player);
        this.bar = bar;
    }

    @Override
    public void run() {
        if (Cooldowns.hasEnded("actionbar_enabled_check")) {
            Cooldowns.start("actionbar_enabled_check", 10, TimeUnit.SECONDS);

            Map<String, String> map = CoreProvider.Cache.Redis.SETTINGS.provide()
                    .fetchSettingsByServer(CoreProvider.getApp().getServer());

            enabled = Boolean.parseBoolean(map.getOrDefault("actionbar_enabled", "false"));
        }

        if (!enabled) {
            return;
        }

        Player player = playerWeakReference.get();

        if (player == null || !player.isOnline()) {
            cancel();
            return;
        }
        
        final String defaultMessage = bar.get();

        if (bar.getCurrentNotificationText() != null) {
            if (bar.getCurrentNotificationTicks() < 20 * 3) {
                new ActionBarMessage()
                        .text(bar.getCurrentNotificationText())
                        .send(player);

                bar.setCurrentNotificationTicks(bar.getCurrentNotificationTicks() + 25);
                return;
            }

            bar.setCurrentNotificationTicks(0);
            bar.setCurrentNotificationText(null);
        }

        if (defaultMessage != null) {
            new ActionBarMessage()
                    .text(defaultMessage)
                    .send(player);
        }
    }
}
