package net.hyze.core.spigot.misc.customitem.data.enchanted_books;

import net.hyze.core.shared.misc.utils.Pair;
import org.bukkit.enchantments.Enchantment;

import java.util.Collections;
import java.util.Map;

public class SimpleVanillaEnchantmentsBook extends VanillaEnchantmentsBook {

    public static String KEY = "simple_vanilla_enchantments_book";

    public SimpleVanillaEnchantmentsBook(Map<Pair<Enchantment, Integer>, Double> enchantments) {

        super(KEY, "Simples", enchantments, Collections.EMPTY_MAP);
    }
}
