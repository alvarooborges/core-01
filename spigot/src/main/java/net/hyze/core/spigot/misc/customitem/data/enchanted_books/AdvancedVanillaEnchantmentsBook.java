package net.hyze.core.spigot.misc.customitem.data.enchanted_books;

import net.hyze.core.shared.misc.utils.Pair;
import org.bukkit.enchantments.Enchantment;

import java.util.Collections;
import java.util.Map;

public class AdvancedVanillaEnchantmentsBook extends VanillaEnchantmentsBook {

    public static String KEY = "advanced_vanilla_enchantments_book";

    public AdvancedVanillaEnchantmentsBook(Map<Pair<Enchantment, Integer>, Double> enchantments) {

        super(KEY, "Avançado", enchantments, Collections.EMPTY_MAP);
    }
}
