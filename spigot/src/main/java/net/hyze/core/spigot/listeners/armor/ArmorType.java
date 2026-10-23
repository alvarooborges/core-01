package net.hyze.core.spigot.listeners.armor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
@Getter
public enum ArmorType {

    HELMET(5, "Capacete"),
    CHESTPLATE(6, "Peitoral"),
    LEGGINGS(7, "Calça"),
    BOOTS(8, "Bota");

    private final int slot;
    private final String displayName;

    /**
     * Attempts to match the ArmorType for the specified ItemStack.
     *
     * @param itemStack The ItemStack to parse the type of.
     * @return The parsed ArmorType. (null if none were found.)
     */
    public static ArmorType matchType(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
            return null;
        }
        String type = itemStack.getType().name();
        if (type.endsWith("_HELMET") || type.endsWith("_SKULL")) {
            return HELMET;
        } else if (type.endsWith("_CHESTPLATE")) {
            return CHESTPLATE;
        } else if (type.endsWith("_LEGGINGS")) {
            return LEGGINGS;
        } else if (type.endsWith("_BOOTS")) {
            return BOOTS;
        } else {
            return null;
        }
    }
}
