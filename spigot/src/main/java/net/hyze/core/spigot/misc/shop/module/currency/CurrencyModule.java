package net.hyze.core.spigot.misc.shop.module.currency;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.misc.shop.module.AbstractModule;
import net.minecraft.server.v1_8_R3.Container;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.*;
import java.util.function.Supplier;

public class CurrencyModule extends AbstractModule {

    private static final Joiner OR_JOINER = Joiner.on(" &7ou ");
    private static final Joiner AND_JOINER = Joiner.on(" &7e ");

    @Getter
    protected final LinkedList<AbstractPrice> prices = Lists.newLinkedList();

    public CurrencyModule(AbstractPrice... prices) {
        Collections.addAll(this.prices, prices);
    }

    @Override
    public State state(User user) {
        for (AbstractPrice price : prices) {
            State state = price.state(user);

            if (state == State.SUCCESS) {
                return state;
            }
        }

        return State.ERROR;
    }

    @Override
    public boolean transaction(User user, Player player, Runnable onSuccess) {

        if (prices.isEmpty()) {
            return false;
        } else if (prices.size() == 1 && !prices.getFirst().needsConfirmation()) {
            AbstractPrice price = Lists.newArrayList(prices).get(0);

            if (price.state(user) == State.SUCCESS) {
                if (price.transaction(user)) {
                    onSuccess.run();
                }
            }

        } else {

            Container container = ((CraftPlayer) player).getHandle().activeContainer;

            Supplier<Inventory> back = () -> {
                if (container != null) {
                    InventoryView inventory = container.getBukkitView();
                    return inventory.getTopInventory();
                }

                return null;
            };

            player.openInventory(new CurrencyConfirmInventory(user, this, onSuccess, back));
        }

        return true;
    }

    @Override
    public String[] defaultLore(User user, State state) {
        if (state != State.SELECTED && state != State.AQUIRED) {
            Set<String> lore = Sets.newLinkedHashSet();
            LinkedList<String> lines = Lists.newLinkedList();
            long discountEnd = 0;

            for (AbstractPrice price : prices) {
                lore.add(price.format());

//                if (price.getDiscountEnd() == -1 || (price.getDiscountEnd() > discountEnd && discountEnd != -1)) {
//                    discountEnd = price.getDiscountEnd();
//                }
            }

            String cost = "&fPreço: " + OR_JOINER.join(lore);

            if (discountEnd > 0) {
                lines.add("&eDesconto válido até:");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(discountEnd);

                lines.add("&e" + calendar.get(Calendar.DATE) + " de "
                        + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                        + ". de " + calendar.get(Calendar.YEAR)
                        + ", às " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ".");
                lines.add("");
                lines.add(cost);

                return lines.stream().toArray(String[]::new);
            }

            return new String[]{"", cost};
        }

        return super.defaultLore(user, state);
    }

    @Override
    public String[] addLore(User user, State state) {
        if (state == State.ERROR) {
            return new String[]{
                    "&cVocê não tem saldo suficiente."
            };
        }

        return super.addLore(user, state);
    }
}
