package net.hyze.core.spigot.misc.customitem.data;

import lombok.Getter;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.misc.combat.CombatManager;
import net.hyze.core.spigot.misc.customitem.CustomItem;
import net.hyze.core.spigot.misc.customitem.CustomItemRegistry;
import net.hyze.core.spigot.misc.enchantments.merchant.MerchantRecipeEvent;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.greenrobot.eventbus.Subscribe;

public class RepairCoinItem extends CustomItem implements Listener {

    @Getter
    private final ItemBuilder itemBuilder;

    public RepairCoinItem() {
        super("custom_item_repair_coin");

        this.itemBuilder = ItemBuilder.of(Material.DOUBLE_PLANT)
                .glowing(true)
                .name("&6Moeda de Reparação")
                .lore(
                        "&7Ao utilizar esta moeda em um item, ele",
                        "&7será reparado completamente!",
                        "",
                        "&eComo utilizar?",
                        "&fClique com esta moeda em cima do",
                        "&fitem que você deseja reparar."
                );
    }

    @Override
    public String getDisplayName() {
        return "&6Moeda de Reparação";
    }

    @Subscribe
    public void on(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Message.ERROR.send(event.getPlayer(), "Você deve utilizar este item clicando sobre outro item.");
        }
    }

    @Subscribe
    public void on(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType() == Material.AIR) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }

        if (CustomItemRegistry.getByItemStack(cursor) != this) {
            return;
        }

        if (canRepair(currentItem)) {

            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            User user = CoreProvider.Cache.Local.USERS.provide().get(player.getName());

            if (CombatManager.isTagged(user)) {
                Message.ERROR.send(player, "Você não pode reparar itens enquanto estiver em combate.");
                return;
            }

            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
            } else {
                event.setCursor(null);
            }

            doRepair(currentItem);
            ((Player) event.getWhoClicked()).updateInventory();

        }
    }

    private boolean canRepair(ItemStack stack) {
        return stack != null && stack.getType() != Material.AIR
                && stack.getDurability() > 0
                && (stack.getType().name().startsWith("DIAMOND_")
                || stack.getType().name().startsWith("IRON_")
                || stack.getType().name().startsWith("GOLD_")
                || stack.getType().name().startsWith("LEATHER_")
                || stack.getType() == Material.BOW);
    }

    private void doRepair(ItemStack stack) {
        ItemBuilder.of(stack, true).durability(0);
    }
}
