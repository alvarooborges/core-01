package net.hyze.core.spigot.misc.enchantments.triggers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerKillPlayerTrigger extends EnchantmentTrigger<PlayerDeathEvent> {

    public PlayerKillPlayerTrigger(PlayerDeathEvent event, Player player, ItemStack item, int level) {
        super(event, player, item, level);
    }
}
