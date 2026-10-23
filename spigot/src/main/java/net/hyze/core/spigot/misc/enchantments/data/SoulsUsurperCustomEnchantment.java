package net.hyze.core.spigot.misc.enchantments.data;

import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.messages.MessageUtils;
import net.hyze.core.shared.misc.utils.RandomUtils;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantment;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantmentSlot;
import net.hyze.core.spigot.misc.enchantments.triggers.EntityDamageByEntityTrigger;
import net.hyze.core.spigot.misc.enchantments.triggers.PlayerKillPlayerTrigger;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemArmor;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class SoulsUsurperCustomEnchantment extends CustomEnchantment {

    public static final String KEY = "custom_souls_usurper_enchantment";

    private final String nbtKey = "souls_usurper_amount";

    public SoulsUsurperCustomEnchantment() {
        super(KEY);
    }

    @Override
    public String getDisplayName() {
        return "Usurpação de Almas";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "Armazene almas ao abater seus inimigos.",
                "Ao completar 10 almas armazenadas, você",
                "possui chance de causar uma danificação",
                "maior em um dos equipamentos do inimigo",
                "e as almas são gastas."
        };
    }

    @Override
    public CustomEnchantmentSlot[] getSlots() {
        return new CustomEnchantmentSlot[]{CustomEnchantmentSlot.AXES};
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Subscribe
    public void on(PlayerKillPlayerTrigger trigger) {
        storeSoulOnAxe(trigger.getItem());
    }

    @Subscribe
    public void on(EntityDamageByEntityTrigger trigger) {
        EntityDamageByEntityEvent event = trigger.getEvent();

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = trigger.getPlayer();
        Player victim = (Player) event.getEntity();

        if (player == victim) {
            return;
        }

        if (CoreConstants.RANDOM.nextInt(100) <= 50) {
            boolean success = trySpentStoredSouls(trigger.getItem());

            if (success) {
                EntityPlayer handle = ((CraftPlayer) victim).getHandle();

                PlayerInventory inventory = handle.inventory;

                int randomArmorIndex = RandomUtils.randomInt(0, 3);

                net.minecraft.server.v1_8_R3.ItemStack armor = inventory.armor[randomArmorIndex];
                if (armor == null || !(armor.getItem() instanceof ItemArmor)) {
                    Message.ERROR.send(player, "Usurpação não encontrou a parte de armadura selecionada!");
                    return;
                }

                int half = (armor.asBukkitMirror().getType().getMaxDurability() - armor.damage) / 2;

                armor.damage(half, handle);

                Message.INFO.send(player, String.format(
                        "A alma de %s foi usurpada e armazenada em sua espada.",
                        victim.getName()
                ));

                Message.INFO.send(victim, String.format(
                        "Sua alma foi usurpada por %s! Melhor verificar sua armadura...",
                        player.getName()
                ));

                player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1F, 0F);
            }
        }
    }

    private void storeSoulOnAxe(ItemStack itemStack) {
        ItemBuilder builder = ItemBuilder.of(itemStack, true);

        if (!builder.hasNbt(nbtKey)) {
            builder.nbt(nbtKey, 0);
        }

        int storedSouls = builder.nbtInt(nbtKey);

        builder.nbt(nbtKey, storedSouls + 1);

        updateItemLore(builder);
    }

    private boolean trySpentStoredSouls(ItemStack itemStack) {
        ItemBuilder builder = ItemBuilder.of(itemStack, true);

        if (!builder.hasNbt(nbtKey)) {
            return false;
        }

        int storedSouls = builder.nbtInt(nbtKey);
        if (storedSouls < 10) {
            return false;
        }

        builder.nbt(nbtKey, storedSouls - 10);

        updateItemLore(builder);

        return true;
    }

    private void updateItemLore(ItemBuilder builder) {
        ItemMeta itemMeta = builder.meta();

        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();

        if (!lore.isEmpty()) {
            lore.removeIf(line -> {
                String rawLine = MessageUtils.stripColor(line);

                return rawLine.startsWith("Almas");
            });
        }

        if (!lore.isEmpty()) {
            String rawLastLine = MessageUtils.stripColor(lore.get(lore.size() - 1)).trim();
            if (!rawLastLine.isEmpty()) {
                lore.add(" ");
            }
        }

        lore.add("&cAlmas Usurpadas: &f" + builder.nbtInt(nbtKey));

        builder.lore(
                true,
                lore.toArray(new String[0])
        );
    }
}
