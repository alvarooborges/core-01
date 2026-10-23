package net.hyze.core.spigot.misc.shop;

import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.misc.customitem.CustomItem;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.shop.module.AbstractModule;
import net.hyze.core.spigot.misc.shop.module.currency.AbstractPrice;
import net.hyze.core.spigot.misc.shop.module.currency.CurrencyModule;
import net.hyze.core.spigot.misc.utils.InventoryUtils;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CustomShopItem extends ShopItem {

    private final CustomItem customItem;
    private final int amount;
    private final FinishHandler onFinish;

    public CustomShopItem(CustomItem customItem, AbstractPrice price, int amount) {
        this(customItem, price, amount, null);
    }

    public CustomShopItem(CustomItem customItem, AbstractPrice price, int amount, FinishHandler onFinish) {
        super(customItem.getDisplayName(), new CurrencyModule(price));

        this.customItem = customItem;
        this.amount = amount;
        this.onFinish = onFinish;
    }

    @Override
    public String[] getDescription() {
        return null;
    }

    @Override
    public ItemBuilder getIcon() {
        return ItemBuilder.of(this.customItem.asItemStack(this.amount));
    }

    @Override
    public void onClick(Player player, User user, AbstractModule.State state, InventoryClickEvent event) {
        if (state == AbstractModule.State.SUCCESS) {

            ItemStack item = this.customItem.asItemStack(this.amount);

            if (!InventoryUtils.fits(player.getInventory(), item)) {
                Message.ERROR.send(player, "Seu inventário está cheio, operação cancelada!");
                player.closeInventory();
                return;
            }

            // int previousBalance = user.getRealCash();

            Runnable onSuccess = () -> {
                player.getInventory().addItem(item);

                Message.SUCCESS.send(player, String.format("Você comprou %sx %s!", this.amount, this.customItem.getDisplayName()));

                /*
                if (this.price instanceof CashPrice) {
                    CashPrice cashPrice = (CashPrice) this.price;


                    Log.Cash.add(
                            user,
                            CoreProvider.getApp().getServer().getId(),
                            this.amount + "x" + this.customItem.getKey().toUpperCase(),
                            -cashPrice.getPrice(),
                            previousBalance,
                            user.getRealCash()
                    );

                }
                 */

                if (this.onFinish != null) {
                    this.onFinish.handle(user, state, true);
                }

            };

            boolean transaction = false;

            for (AbstractModule module : modules) {
                transaction |= module.transaction(user, player, onSuccess);
            }

            if (!transaction) {
                if (this.onFinish != null) {
                    this.onFinish.handle(user, state, false);
                }
            }
        }
    }

    public interface FinishHandler {

        void handle(User user, AbstractModule.State state, boolean transaction);
    }
}
