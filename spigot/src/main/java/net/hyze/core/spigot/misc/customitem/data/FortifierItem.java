package net.hyze.core.spigot.misc.customitem.data;

import lombok.Getter;
import net.hyze.core.spigot.misc.customitem.CraftableCustomItem;
import net.hyze.core.spigot.misc.customitem.CustomItem;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class FortifierItem extends CustomItem implements CraftableCustomItem {

    @Getter
    private final ItemBuilder itemBuilder;

    public static String KEY = "custom_item_fortifier";

    public FortifierItem() {
        super(KEY);

        this.itemBuilder = ItemBuilder.of(new ItemStack(Material.FIREWORK_CHARGE))
                .name(getDisplayName())
                .flags(ItemFlag.values())
                .lore("&7Junte esse item com um")
                .lore("&7Tijolo de Pedra Rachado")
                .lore("&7e obtenha um Tijolo de")
                .lore("&7Pedra novinho em folha.");
    }

    @Override
    public String getDisplayName() {
        return "&5Fortificador";
    }
}
