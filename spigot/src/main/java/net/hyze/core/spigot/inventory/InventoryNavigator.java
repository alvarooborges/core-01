package net.hyze.core.spigot.inventory;

import net.minecraft.server.v1_8_R3.IInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.WeakHashMap;

public class InventoryNavigator {

    private static final Map<String, Inventory> NAVIGATOR_MAP = new WeakHashMap<>();

    public static Inventory getBack(Player player) {
        return NAVIGATOR_MAP.get(player.getName());
    }

    public static void computeBack(Player player, Inventory inventory) {
        Inventory previousInventory = NAVIGATOR_MAP.get(player.getName());

        if (previousInventory != null) {

            boolean isEqual = isEqual(previousInventory, inventory);

            if (isEqual) {
                return;
            }

        }

        NAVIGATOR_MAP.put(player.getName(), inventory);
    }

    public static void remove(Player player) {
        NAVIGATOR_MAP.remove(player.getName());
    }

    private static boolean isEqual(Inventory invA, Inventory invB) {
        IInventory aIInventory = ((CraftInventory) invA).getInventory();

        CustomInventory aParent = (CustomInventory) ((CustomInventory.MinecraftInventory) aIInventory)
                .getParent();

        IInventory bIInventory = ((CraftInventory) invB).getInventory();

        CustomInventory bParent = (CustomInventory) ((CustomInventory.MinecraftInventory) bIInventory)
                .getParent();

        return aParent.getClass().isAssignableFrom(bParent.getClass());
    }

}
