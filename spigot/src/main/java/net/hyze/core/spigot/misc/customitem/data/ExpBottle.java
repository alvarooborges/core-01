package net.hyze.core.spigot.misc.customitem.data;

import lombok.Getter;
import net.hyze.core.spigot.CoreSpigotConstants;
import net.hyze.core.spigot.misc.customitem.CustomItem;
import net.hyze.core.spigot.misc.customitem.INonStackable;
import net.hyze.core.spigot.misc.utils.InventoryUtils;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.core.spigot.misc.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ExpBottle extends CustomItem implements INonStackable, Listener {

    public static String KEY = "exp_bottle";

    @Getter
    private final ItemBuilder itemBuilder = new ItemBuilder(Material.EXP_BOTTLE);

    public ExpBottle() {
        this(7);
    }

    public ExpBottle(int exp) {
        super(KEY);

        itemBuilder.name("&a" + CoreSpigotConstants.TRANSLATE_ITEM.get(itemBuilder.make()))
                .amount(1)
                .lore("&7XP: &f" + exp)
                .lore(String.format("&7Níveis: &f%.1f", PlayerUtils.getLevelFromExp(exp)))
                .nbt("exp_bottle_value", exp);
    }

    @Override
    public String getDisplayName() {
        return itemBuilder.name();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(ProjectileLaunchEvent event) {

        Projectile projectile = event.getEntity();

        if (projectile instanceof ThrownExpBottle) {

            ProjectileSource source = projectile.getShooter();

            if (source instanceof Player) {

                Player player = (Player) source;

                ItemBuilder item = ItemBuilder.of(player.getItemInHand());

                if (item.hasNbt("exp_bottle_value")) {
                    event.setCancelled(true);

                    InventoryUtils.subtractOneOnHand((Player) event.getEntity().getShooter());

                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 10, 0);
                    PlayerUtils.changeExp(player, item.nbtInt("exp_bottle_value"));
                }
            }
        }
    }
}
