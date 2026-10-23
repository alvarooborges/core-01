package net.hyze.core.spigot.misc.customitem.data.compacted;

import net.hyze.core.spigot.CoreSpigotConstants;
import net.hyze.core.spigot.misc.customitem.CustomItem;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import org.bukkit.Material;

public class MagmaCreamCompacted extends CustomItem {

    public static String KEY = "magma_cream_compacted";

    public MagmaCreamCompacted() {
        super(KEY);
    }

    @Override
    protected ItemBuilder getItemBuilder() {
        return ItemBuilder.of(Material.MAGMA_CREAM)
                .name(getDisplayName())
                .nbt(CoreSpigotConstants.NBTKeys.CUSTOM_AMOUNT, 9)
                .glowing(true);
    }

    @Override
    public String getDisplayName() {
        return "&bCreme de Magma Compactado";
    }
}