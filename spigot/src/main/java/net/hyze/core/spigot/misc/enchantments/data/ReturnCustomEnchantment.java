package net.hyze.core.spigot.misc.enchantments.data;

import net.hyze.core.spigot.listeners.armor.ArmorType;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantment;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantmentSlot;
import net.hyze.core.spigot.misc.enchantments.triggers.PlayerItemDamageTrigger;
import net.hyze.core.spigot.misc.message.Message;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.greenrobot.eventbus.Subscribe;

public class ReturnCustomEnchantment extends CustomEnchantment {

    public static final String KEY = "custom_return_enchantment";

    public ReturnCustomEnchantment() {
        super(KEY);
    }

    @Override
    public String getDisplayName() {
        return "Retorno";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "&7Equipamentos com este encantamento,",
                "&7ao serem partidos, se renovam sem",
                "&7o Retorno equipado."
        };
    }

    @Override
    public CustomEnchantmentSlot[] getSlots() {
        return new CustomEnchantmentSlot[]{CustomEnchantmentSlot.ARMOR};
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Subscribe
    public void on(PlayerItemDamageTrigger trigger) {

        PlayerItemDamageEvent event = trigger.getEvent();

        if (event.isCancelled()) {
            return;
        }

        ItemStack item = trigger.getItem();

        int takenDamage = (item.getType().getMaxDurability() - item.getDurability());

        if (takenDamage - event.getDamage() <= 10) {
            event.setCancelled(true);

            this.remove(item);

            Player player = event.getPlayer();

            Message.INFO.send(player, String.format(
                    "Encantamento 'Retorno' aplicado à %s!",
                    ArmorType.matchType(item).getDisplayName()
            ));

            item.setDurability((short) 0);

            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1F, 0F);
        }
    }
}
