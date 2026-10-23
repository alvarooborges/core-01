package net.hyze.core.spigot.misc.customitem.data;

import lombok.Getter;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.misc.cooldowns.UserCooldowns;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotConstants;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.misc.customitem.CustomItem;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.InventoryUtils;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.greenrobot.eventbus.Subscribe;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

public class LauncherItem extends CustomItem {

    private static Map<Player, BukkitTask> NCP_EXEMPTION_TASKS = new WeakHashMap<>();

    @Getter
    private final ItemBuilder itemBuilder;

    public static String KEY = "custom_item_laucher";

    public LauncherItem() {
        super(KEY);

        this.itemBuilder = ItemBuilder.of(Material.FIREWORK)
                .glowing(true)
                .name("&aLançador")
                .lore("Ao utilizar este item você", "será arremessado em 60", "blocos para cima!");
    }

    @Override
    public String getDisplayName() {
        return "&aLançador";
    }

    private static final WeakHashMap<Player, BukkitTask> CAN_FLY_TASK = new WeakHashMap<>();


    @Subscribe
    public void on(PlayerInteractEvent event) {
        if (handleLauncher(event)) {
            InventoryUtils.subtractOneOnHand(event);
        }
    }


    public static boolean handleLauncher(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();

        User user = CoreProvider.Cache.Local.USERS.provide().get(player.getName());

        if (player.getLocation().getY() + 45 >= 256) {
            Message.ERROR.send(player, "Você está muito alto.");
            return false;
        }

        if (!UserCooldowns.hasEnded(user, "user-launcher-item")) {
            Message.ERROR.send(player, String.format(
                    "&cAguarde %s para usar o lançador novamente",
                    UserCooldowns.getFormattedTimeLeft(user, "user-launcher-item")
            ));
            return false;
        }

        player.setAllowFlight(true);

        BukkitTask task = CAN_FLY_TASK.put(player, new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.setAllowFlight(false);
                }

                CAN_FLY_TASK.remove(player);
            }
        }.runTaskLater(CoreSpigotPlugin.getInstance(), 20 * 3));

        if (task != null) {
            task.cancel();
        }

        double up = 3.5;

        player.setVelocity(new Vector(0, up, 0));

        player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 5, 0);

        Bukkit.getScheduler().runTaskAsynchronously(CoreSpigotPlugin.getInstance(), () -> {
            for (int i = 0; i < 200; i++) {
                player.getWorld().playEffect(player.getLocation(), Effect.FIREWORKS_SPARK, 1, 5);
            }
        });

        player.setMetadata(CoreSpigotConstants.NBTKeys.PLAYER_FALL_DAMAGE_BYPASS, new FixedMetadataValue(CoreSpigotPlugin.getInstance(), true));

        UserCooldowns.start(user, "user-launcher-item", 3, TimeUnit.SECONDS);

        return true;
    }
}
