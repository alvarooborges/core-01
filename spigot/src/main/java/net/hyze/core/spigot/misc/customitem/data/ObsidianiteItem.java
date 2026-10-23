package net.hyze.core.spigot.misc.customitem.data;

import lombok.Getter;
import net.hyze.core.spigot.misc.customitem.CraftableCustomItem;
import net.hyze.core.spigot.misc.customitem.CustomItem;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ObsidianiteItem extends CustomItem implements CraftableCustomItem {

    @Getter
    private final ItemBuilder itemBuilder;

    public static String KEY = "custom_item_obsidianite";

    public ObsidianiteItem() {
        super(KEY);

        this.itemBuilder = ItemBuilder.of(new ItemStack(Material.COAL, 1, (byte) 1))
                .glowing(true)
                .name(getDisplayName())
                .lore("&7Junte esse minério a uma Tijolo de Pedra")
                .lore("&7e consiga uma Obsidiana Reforçada!");
    }

    @Override
    public String getDisplayName() {
        return "&5Obsidianita";
    }
}
