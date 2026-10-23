package net.hyze.core.spigot.misc.enchantments.data;

import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.misc.cooldowns.UserCooldowns;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantment;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantmentSlot;
import net.hyze.core.spigot.misc.enchantments.triggers.EntityDamageByEntityTrigger;
import net.hyze.core.spigot.misc.message.Message;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;

public class HandCuffsCustomEnchantment extends CustomEnchantment {

    public static final String KEY = "custom_handcuffs_enchantment";

    private static final String COOLDOWN_KEY = "recently-handcuffed";

    private final Int2LongMap handCuffed = new Int2LongOpenHashMap();

    public HandCuffsCustomEnchantment() {
        super(KEY);
    }

    @Override
    public String getDisplayName() {
        return "Algemas";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "Faça com que o inimigo que está",
                "sendo atingido fique impossibilitado",
                "de quebrar ou colocar blocos durante",
                "10 segundos."
        };
    }

    @Override
    public CustomEnchantmentSlot[] getSlots() {
        return new CustomEnchantmentSlot[]{CustomEnchantmentSlot.SWORD};
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Subscribe
    public void on(EntityDamageByEntityTrigger trigger) {
        EntityDamageByEntityEvent event = trigger.getEvent();

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        User victimUser = CoreProvider.Cache.Local.USERS.provide().get(victim.getName());

        if (!UserCooldowns.hasEnded(
                victimUser,
                COOLDOWN_KEY)
        ) {
            return;
        }

        Player player = trigger.getPlayer();

        if (CoreConstants.RANDOM.nextInt(100) <= 18) {

            int id = victimUser.getId();

            handCuffed.put(id, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10));

            UserCooldowns.start(victimUser, COOLDOWN_KEY, 30L, TimeUnit.SECONDS);

            Message.INFO.send(player, String.format(
                    "Você algemou %s! Ele está impossibilitado de colocar/remover blocos por 10 segundos.",
                    victim.getName()
            ));

            Message.INFO.send(victim, String.format(
                    "%s algemou você! Você está impossibilitado de colocar/remover blocos por 10 segundos.",
                    player.getName()
            ));

            victim.playSound(victim.getLocation(), Sound.DOOR_OPEN, 1F, 0F);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerQuitEvent event) {
        handCuffed.remove(
                (int) CoreProvider.Cache.Local.USERS.provide().get(event.getPlayer().getName()).getId()
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(BlockPlaceEvent event) {
        Integer userId = CoreProvider.Cache.Local.USERS.provide().get(event.getPlayer().getName()).getId();

        long timeLeft = handCuffed.get((int) userId);
        if (timeLeft == handCuffed.defaultReturnValue()) {
            return;
        }

        if (timeLeft < System.currentTimeMillis()) {
            handCuffed.remove((int) userId);
            return;
        }

        event.setCancelled(true);
        event.setBuild(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(BlockBreakEvent event) {
        Integer userId = CoreProvider.Cache.Local.USERS.provide().get(event.getPlayer().getName()).getId();

        long timeLeft = handCuffed.get((int) userId);
        if (timeLeft == handCuffed.defaultReturnValue()) {
            return;
        }

        if (timeLeft < System.currentTimeMillis()) {
            handCuffed.remove((int) userId);
            return;
        }

        event.setCancelled(true);
    }
}
