package net.hyze.core.spigot.misc.enchantments.data;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantment;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantmentSlot;
import net.hyze.core.spigot.misc.enchantments.triggers.EntityDamageByEntityTrigger;
import net.hyze.core.spigot.misc.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.greenrobot.eventbus.Subscribe;

public class RaptureCustomEnchantment extends CustomEnchantment {

    public static final String KEY = "custom_rapture_enchantment";

    public RaptureCustomEnchantment() {
        super(KEY);
    }

    @Override
    public String getDisplayName() {
        return "Arrebatamento";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "Ao acertar uma flecha no inimigo,",
                "existe uma chance de roubar corações",
                "dele. Sendo assim, você recebe os",
                "os corações que foram roubados."
        };
    }

    @Override
    public CustomEnchantmentSlot[] getSlots() {
        return new CustomEnchantmentSlot[]{CustomEnchantmentSlot.BOW};
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    private final Int2DoubleMap takenHearts = new Int2DoubleOpenHashMap();

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

        int level = trigger.getLevel();
        if (CoreConstants.RANDOM.nextInt(100) > (15 * level)) {
            int victimId = CoreProvider.Cache.Local.USERS.provide().get(victim.getName()).getId();

            if (takenHearts.containsKey(victimId)) {
                return;
            }

            Message.INFO.send(victim, "Você foi arrebatado!");
            Message.INFO.send(player, String.format(
                    "Você arrebatou %s",
                    victim.getName()
            ));

            double heartsTaken = 0.5 * level + (level >= 3 ? 1.0 : 0.0);

            int seconds = 5 * level;

            if (victim.getMaxHealth() - heartsTaken <= 0.0) {
                heartsTaken -= 0.5;
            }

            double damagerMaxHealth = player.getMaxHealth();
            double victimMaxHealth = victim.getMaxHealth();

            victim.setMaxHealth(victimMaxHealth - heartsTaken);
            victim.setHealth(victim.getMaxHealth());

            player.setMaxHealth(damagerMaxHealth + heartsTaken);
            player.setHealth(player.getMaxHealth());

            int damagerId = CoreProvider.Cache.Local.USERS.provide().get(player.getName()).getId();
            takenHearts.put(
                    damagerId,
                    damagerMaxHealth
            );

            takenHearts.put(
                    victimId,
                    victimMaxHealth
            );

            Bukkit.getScheduler().runTaskLater(
                    CoreSpigotPlugin.getInstance(),
                    () -> {

                        if (player.isOnline()) {
                            player.setMaxHealth(damagerMaxHealth);
                            player.setHealth(damagerMaxHealth);

                            takenHearts.remove(damagerId);
                        }

                        if (victim.isOnline()) {
                            victim.setMaxHealth(victimMaxHealth);
                            victim.setHealth(victimMaxHealth);

                            takenHearts.remove(victimId);
                        }

                    },
                    seconds * 20L
            );
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        double takenHearts = this.takenHearts.remove(
                (int) CoreProvider.Cache.Local.USERS.provide().get(player.getName()).getId()
        );

        if (takenHearts != this.takenHearts.defaultReturnValue()) {
            player.setMaxHealth(takenHearts);
        }
    }
}
