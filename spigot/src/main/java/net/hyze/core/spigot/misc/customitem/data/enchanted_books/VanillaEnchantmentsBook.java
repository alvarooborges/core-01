package net.hyze.core.spigot.misc.customitem.data.enchanted_books;

import lombok.Getter;
import net.hyze.core.shared.misc.utils.Pair;
import net.hyze.core.shared.misc.utils.WeightedCollection;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantment;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.InventoryUtils;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.core.spigot.misc.utils.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.greenrobot.eventbus.Subscribe;

import java.util.Map;
import java.util.UUID;

public abstract class VanillaEnchantmentsBook extends AbstractRandomEnchantmentsBook {

    @Getter
    protected final String name;

    public VanillaEnchantmentsBook(String key,
                                   String name,
                                   Map<Pair<Enchantment, Integer>, Double> enchantments,
                                   Map<Pair<CustomEnchantment, Integer>, Double> customEnchantments) {

        super(key, enchantments, customEnchantments);

        this.name = name;

        this.itemBuilder.name("&7" + getDisplayName())
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .glowing(false)
                .lore(true,
                        "&7Tipo: &f" + name
                );
    }

    @Override
    public String getDisplayName() {
        return "&eLivro de Encantamentos";
    }

    @Subscribe
    public void on(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        WeightedCollection<Pair<?, Integer>> randomList = getRandomList(event.getItem());

        Player player = event.getPlayer();

        if (randomList.isEmpty()) {
            Message.ERROR.send(player, " OPS! Não achamos os encantamentos :(");
            return;
        }

        ItemStack itemChecker = ItemBuilder.of(Material.BARRIER).name(UUID.randomUUID().toString()).make();

        if (!InventoryUtils.fits(player.getInventory(), itemChecker)) {
            Message.ERROR.send(player, "Seu inventário está cheio.");
            return;
        }

        Pair<?, Integer> pair = randomList.next().getObject();

        ItemStack reward;

        if (pair.getLeft() instanceof Enchantment) {
            Enchantment enchantment = (Enchantment) pair.getLeft();

            reward = new ItemStack(Material.ENCHANTED_BOOK);

            ItemStackUtils.addBookEnchantment(reward, enchantment, pair.getRight());

        } else if (pair.getLeft() instanceof CustomEnchantment) {
            CustomEnchantment enchantment = (CustomEnchantment) pair.getLeft();

            reward = enchantment.getBook(pair.getRight(), 1);
        } else {
            Message.ERROR.send(player, "Algo de errado aconteceu! Entre em contato com algum staff.");
            return;
        }

        ItemStack inHand = player.getItemInHand();

        boolean subtract = InventoryUtils.subtractOne(player, inHand);

        if (subtract) {
            player.getInventory().addItem(reward);
            Message.SUCCESS.send(player, "Você achou 1x Livro " + getRewardName(pair) + ".");
        }
    }
}
