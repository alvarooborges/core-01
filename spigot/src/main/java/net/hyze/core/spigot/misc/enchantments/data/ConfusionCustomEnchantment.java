package net.hyze.core.spigot.misc.enchantments.data;

import net.hyze.core.shared.CoreConstants;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantment;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantmentSlot;
import net.hyze.core.spigot.misc.enchantments.triggers.EntityDamageByEntityTrigger;
import net.hyze.core.spigot.misc.message.Message;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.greenrobot.eventbus.Subscribe;

import java.util.*;

public class ConfusionCustomEnchantment extends CustomEnchantment {

    public static final String KEY = "custom_confusion_enchantment";

    public ConfusionCustomEnchantment() {
        super(KEY);
    }

    @Override
    public String getDisplayName() {
        return "Confusão";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "Atinja seu inimigo com uma flecha",
                "e tenha a chance de embaralhar o",
                "inventário dele."
        };
    }

    @Override
    public CustomEnchantmentSlot[] getSlots() {
        return new CustomEnchantmentSlot[]{CustomEnchantmentSlot.BOW};
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Subscribe
    public void on(EntityDamageByEntityTrigger trigger) {
        EntityDamageByEntityEvent event = trigger.getEvent();

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Arrow)) {
            return;
        }

        Player player = trigger.getPlayer();
        Player victim = (Player) event.getEntity();

        if (player == victim) {
            return;
        }

        if (CoreConstants.RANDOM.nextInt(100) <= 25) {

            Map<Integer, ItemStack> indexesMap = new HashMap<>();

            for (int i = 0; i < victim.getInventory().getSize(); i++) {
                ItemStack content = victim.getInventory().getItem(i);

                if (content == null || content.getType() == Material.AIR) {
                    continue;
                }

                indexesMap.put(i, content);
            }

            List<Integer> keyList = new ArrayList<>(indexesMap.keySet());
            List<ItemStack> valuesList = new ArrayList<>(indexesMap.values());

            Collections.shuffle(valuesList);

            for (int i = 0; i < valuesList.size(); i++) {
                victim.getInventory().setItem(
                        keyList.get(i),
                        valuesList.get(i)
                );
            }

            Message.INFO.send(victim, String.format(
                    "%s te deixou confuso e embaralhou seu inventário!",
                    player.getName()
            ));

            Message.INFO.send(player, String.format(
                    "Você deixou %s confuso e embaralhou o inventário dele!",
                    victim.getName()
            ));

            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks++ >= 5) {
                        this.cancel();
                        return;
                    }

                    if (player.isOnline()) {
                        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 0F);
                    }
                }
            }.runTaskTimer(CoreSpigotPlugin.getInstance(), 3L, 3L);
        }
    }
}
