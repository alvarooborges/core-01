package net.hyze.core.spigot.misc.customitem.data.enchanted_books;

import net.hyze.core.shared.misc.utils.Pair;
import org.bukkit.enchantments.Enchantment;

import java.util.Collections;
import java.util.Map;

public class NormalVanillaEnchantmentsBook extends VanillaEnchantmentsBook {

    public static String KEY = "normal_vanilla_enchantments_book";

    public NormalVanillaEnchantmentsBook(Map<Pair<Enchantment, Integer>, Double> enchantments) {

        super(KEY, "Normal", enchantments, Collections.EMPTY_MAP);
    }
}
